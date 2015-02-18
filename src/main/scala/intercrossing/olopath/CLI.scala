package intercrossing.olopath

import java.io.{PrintWriter, File}
import com.thinkaurelius.titan.core.TitanVertex
import intercrossing.olopath.titan._
import scala.collection.JavaConversions._


object CLI {



  def getWorkingDirectory: File = {
    val jar = new File(this.getClass.getProtectionDomain()
      .getCodeSource().getLocation.toURI)

    if (jar.getName.endsWith(".jar")) {
      jar.getParentFile
    } else {
      jar.getParentFile.getParentFile.getParentFile //for sbt
    }
  }

  def printUsage(workingDirectory: File): Unit = {
    val usage = """|
      |OLOPath.db
      |
      |olo import all
      |download and import to database all data sources UniprotKB, GeneSetDB and BioSystems.
      |
      |olo cluster BioSystems GeneSetDB -d0 <distance> -min <minSize> [-max <maxSize>] -o <file>
      |cluster gene sets from BioSystems and GeneSetDB with specified distance d0,
      |gene sets that smaller than minSize and larger than maxSize will be filtered.
      |
      |olo cluster GeneSetDB -d0 <distance> -min <minSize> [-max <maxSize>] -o <file>
      |cluster gene sets from GeneSetDB with specified distance d0,
      |gene sets that smaller than minSize and larger than maxSize will be filtered.
      |
      |olo cluster BioSystems -d0 <distance> -min <minSize> [-max <maxSize>] -o <file>
      |cluster gene sets from GeneSetDB with specified distance d0,
      |gene sets that smaller than minSize and larger than maxSize will be filtered.
      |
      |olo compare BioSystems GeneSetDB -d0 <distance> -min <minSize> [-max <maxSize>] -unique <uniqueFile> -common <commonFile>
      |map gene sets from BioSystems against GeneSetDB using specified distance d0,
      |gene sets from BioSystems that couldn't be mapped to any gene set from GeneSetDB written to uniqueFile
      |and to commonFile otherwise.
      |
      |olo compare GeneSetDB BioSystems -d0 <distance> -min <minSize> [-max <maxSize>] -unique <uniqueFile> -common <commonFile>
      |map gene sets from GeneSetDB against BioSystems using specified distance d0,
      |gene sets from GeneSetDB that couldn't be mapped to any gene set from GeneSetDB written to uniqueFile
      |and to commonFile otherwise.
      |
      |olo import UniprotKB
      |download and import UniprotKB.
      |
      |olo import GeneSetDB
      |download and import GeneSetDB.
      |
      |olo import BioSystems
      |download and import BioSystems.
      |
      |olo database status
      |print status of database.
    """.stripMargin

    val usageFile = new File(workingDirectory, "usage")
    if(usageFile.exists()) {
      println(io.Source.fromFile(usageFile).getLines().mkString)
    } else {
      println(usage)
    }

  }


  def main(args: Array[String]): Unit = {
    args.toList match {
      case Nil => println("command not specified")
      case "import" :: "all" :: Nil => {
        val database = Database.create(delete = true, getWorkingDirectory)
        database.downloadAndImportAll(getWorkingDirectory)
        database.shutdown()
      }

      case "import" :: "UniprotKB" :: Nil => {
        val uniprotFile = new File(getWorkingDirectory, "uniprot_sprot_human.dat.gz")
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importUniprot(uniprotFile)
        database.shutdown()
      }

      case "import" :: "GeneSetDB" :: Nil => {
        val file = new File(getWorkingDirectory, "download-gmt_h.txt")
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importGeneSetDB(file)
        database.shutdown()
      }

      case "import" :: "BioSystems" :: Nil => {
        val taxonomyFile = new File(getWorkingDirectory, "biosystems_taxonomy.gz")
        val database = Database.create(delete = false, getWorkingDirectory)

        val genesFile = new File("biosystems_gene.gz")
        database.importBioSystems(taxonomyFile, genesFile)

        database.shutdown()
      }

      case "database" :: "status" :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        println("imported modules:")
        if (database.isUniprotImported) {
          println("UniprotKB")
        }
        if (database.isHG19Imported) {
          println("hg19 positions")
        }
        if (database.isHG38Imported) {
          println("hg38 positions")
        }
        if (database.isGeneSetDBImported) {
          println("GeneSetDB")
        }
        if (database.isBioSystemsImported) {
          println("BioSystems")
        }
        database.shutdown()
      }

      case "cluster" :: "BioSystems" :: "GeneSetDB" :: "-d0" :: d0 :: "-min" :: minSize :: "-o" :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt)
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt)
        val union = bioSystems ++= geneSetDB
        val res = GeneSetClustering.cluster(union, d0.toDouble)
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }

      case "cluster" :: "BioSystems" :: "GeneSetDB" :: "-d0" :: d0 :: "-min" :: minSize :: "-max" :: maxSize :: "-o" :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        val union = bioSystems ++= geneSetDB
        val res = GeneSetClustering.cluster(union, d0.toDouble, Some(maxSize.toInt))
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }

      case "cluster" :: "BioSystems" :: "-d0" :: d0 :: "-min" :: minSize :: "-o" :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt)
        val res = GeneSetClustering.cluster(bioSystems, d0.toDouble)
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }

      case "cluster" :: "BioSystems" :: "-d0" :: d0 :: "-min" :: minSize :: "-max" :: maxSize :: "-o" :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        val res = GeneSetClustering.cluster(bioSystems, d0.toDouble, Some(maxSize.toInt))
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }

      case "cluster" :: "GeneSetDB" ::  "-d0" :: d0 :: "-min" :: minSize :: "-o" :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt)
        val res = GeneSetClustering.cluster(geneSetDB, d0.toDouble)
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }

      case "cluster" :: "GeneSetDB" ::  "-d0" :: d0 :: "-min" :: minSize :: "-max" :: maxSize :: "-o" :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        val res = GeneSetClustering.cluster(geneSetDB, d0.toDouble, Some(maxSize.toInt))
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }


      case "compare" :: "BioSystems" :: "GeneSetDB" :: "-d0" :: d0 :: "-min" :: minSize :: "-unique" :: unique :: "-common" :: common :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val d0d = d0.toDouble
        val minSizeI = minSize.toInt
        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt)
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt)
        GeneSetClustering.compare(bioSystems, geneSetDB, d0d, new File(unique), new File(common))
        database.shutdown()
      }

      case "compare" :: "BioSystems" :: "GeneSetDB" :: "-d0" :: d0 :: "-min" :: minSize :: "-max" :: maxSize :: "-unique" :: unique :: "-common" :: common :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val d0d = d0.toDouble
        val minSizeI = minSize.toInt
        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        GeneSetClustering.compare(bioSystems, geneSetDB, d0d, new File(unique), new File(common))
        database.shutdown()
      }

      case "compare" :: "GeneSetDB" :: "BioSystems" :: "-d0" :: d0 :: "-min" :: minSize :: "-unique" :: unique :: "-common" :: common :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val d0d = d0.toDouble
        val minSizeI = minSize.toInt
        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt)
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt)
        GeneSetClustering.compare(bioSystems, geneSetDB, d0d, new File(unique), new File(common))
        database.shutdown()
      }

      case "compare" :: "GeneSetDB" :: "BioSystems" :: "-d0" :: d0 :: "-min" :: minSize :: "-max" :: maxSize :: "-unique" :: unique :: "-common" :: common :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val d0d = d0.toDouble
        val minSizeI = minSize.toInt
        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        GeneSetClustering.compare(bioSystems, geneSetDB, d0d, new File(unique), new File(common))
        database.shutdown()
      }

      case "help" :: Nil => printUsage(getWorkingDirectory)

      case _ => printUsage(getWorkingDirectory)


    }

  }
}
