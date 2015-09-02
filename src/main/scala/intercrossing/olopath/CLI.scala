package intercrossing.olopath

import java.io.{PrintWriter, File}
import com.thinkaurelius.titan.core.TitanVertex
import intercrossing.olopath.titan._
import scala.collection.JavaConversions._
import scala.collection.mutable


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
      |olo import <name> <file>
      |import gene sets from the file in BioSystems format in the database.
      |
      |olo database status
      |print status of database.
    """.stripMargin

    val usageFile = new File(workingDirectory, "usage")
    if(usageFile.exists()) {
      println(io.Source.fromFile(usageFile).getLines().mkString(System.lineSeparator()))
    } else {
      println(usage)
    }

  }

  def parseDatabases(databases: String): List[GeneSetDatabase] = {
    databases.split(',').map(GeneSetDatabase(_)).toList
  }


  def main(args: Array[String]): Unit = {
    //println(args.toList)
    args.toList match {

      case "import" :: "all" :: Nil => {
        val database = Database.create(delete = true, getWorkingDirectory)
        database.downloadAndImportAll(getWorkingDirectory)
        database.shutdown()
      }


      case "database" :: "reset" :: Nil => {
        val database = Database.create(delete = true, getWorkingDirectory)
        database.shutdown()
      }

      case "import" :: "UniprotKB" :: Nil => {
        val uniprotFile = new File(getWorkingDirectory, "uniprot_sprot_human.dat.gz")
        val database = Database.create(delete = false, getWorkingDirectory)
        Download.downloadUniprot(uniprotFile)
        database.importUniprot(uniprotFile)
        database.shutdown()
      }

      case "import" :: "IntPath" :: Nil => {
        val intPath = new File("sapiens.zip")
        val database = Database.create(delete = false, getWorkingDirectory)
        Download.downloadIntPath(intPath)
        database.importIntPath(intPath)
        database.shutdown()
      }

      case "import" :: "GeneSetDB" :: Nil => {
        val file = new File(getWorkingDirectory, "download-gmt_h.txt")
        val database = Database.create(delete = false, getWorkingDirectory)
        Download.downloadGeneSetDB(file)
        database.importGeneSetDB(file)
        database.shutdown()
      }

      case "import" :: "BioSystems" :: Nil => {
        val taxonomyFile = new File(getWorkingDirectory, "biosystems_taxonomy.gz")
        val database = Database.create(delete = false, getWorkingDirectory)

        val genesFile = new File("biosystems_gene.gz")
        Download.downloadBioSystems(taxonomyFile, genesFile)
        database.importBioSystems(taxonomyFile, genesFile)

        database.shutdown()
      }

      case "import" :: name :: file :: Nil => {
        val databaseFile = new File(file)
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importCustom(name, databaseFile)
        database.shutdown()
      }

      case "database" :: "status" :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        println("imported modules:")
        database.listImportedModules().foreach { moduleName =>
          println(moduleName)
        }
        database.graph.commit()
        database.shutdown()
      }

      case "cluster" :: databases0 :: "-d0" :: d0 :: "-min" :: minSize :: "-max" :: maxSize :: "-o" :: out :: Nil => {

        val database = Database.create(delete = false, getWorkingDirectory)
        val databases = parseDatabases(databases0)
        val allGeneSets = new mutable.HashMap[String, mutable.ArrayBuffer[Long]]
        databases.foreach { geneSetDatabase =>
          val geneSets = geneSetDatabase.getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
          geneSets.foreach { case (name, genes) =>
            allGeneSets.get(name) match {
              case None => allGeneSets.put(name, genes)
              case Some(oldGenes) => {
                println("warning: gene set " + name + " is already loaded merging")
                allGeneSets.put(name, SetUtils.union(oldGenes, genes))
              }
            }
          }
        }

        val res = GeneSetClustering.cluster(allGeneSets, d0.toDouble, Some(maxSize.toInt))
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }


      case "cluster" :: databases0 :: "-d0" :: d0 :: "-min" :: minSize :: "-o" :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val databases = parseDatabases(databases0)
        val allGeneSets = new mutable.HashMap[String, mutable.ArrayBuffer[Long]]
        databases.foreach { geneSetDatabase =>
          val geneSets = geneSetDatabase.getGeneSets(database.graph, minSize.toInt, None)
          geneSets.foreach { case (name, genes) =>
            allGeneSets.get(name) match {
              case None => allGeneSets.put(name, genes)
              case Some(oldGenes) => {
                println("warning: gene set " + name + " is already loaded merging")
                allGeneSets.put(name, SetUtils.union(oldGenes, genes))
              }
            }
          }
        }
        val res = GeneSetClustering.cluster(allGeneSets, d0.toDouble, None)
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }


      case "compare" :: database1 :: database2 :: "-d0" :: d0 :: "-min" :: minSize :: "-unique" :: unique :: "-common" :: common :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val d0d = d0.toDouble
        val minSizeI = minSize.toInt
        val geneSets1 = GeneSetDatabase.apply(database1).getGeneSets(database.graph, minSize.toInt)
        val geneSets2 = GeneSetDatabase.apply(database2).getGeneSets(database.graph, minSize.toInt)
        GeneSetClustering.compare(geneSets1, geneSets2, d0d, new File(unique), new File(common))
        database.shutdown()
      }

      case "compare" :: database1 :: database2  :: "-d0" :: d0 :: "-min" :: minSize :: "-max" :: maxSize :: "-unique" :: unique :: "-common" :: common :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val d0d = d0.toDouble
        val minSizeI = minSize.toInt
        val geneSets1 = GeneSetDatabase.apply(database1).getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        val geneSets2 = GeneSetDatabase.apply(database2).getGeneSets(database.graph, minSize.toInt, Some(maxSize.toInt))
        GeneSetClustering.compare(geneSets1, geneSets2, d0d, new File(unique), new File(common))
        database.shutdown()
      }


      case "help" :: Nil => printUsage(getWorkingDirectory)

      case _ => printUsage(getWorkingDirectory)


    }

  }
}
