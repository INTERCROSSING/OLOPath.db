package intercrossing.olopath

import java.io.{PrintWriter, File}
import com.thinkaurelius.titan.core.TitanVertex
import intercrossing.olopath.titan._
import scala.collection.JavaConversions._

object CLI {

  def getWorkingDirectory: File = {
    val jar = new File(this.getClass.getProtectionDomain()
      .getCodeSource().getLocation.toURI)

    if(jar.getName.endsWith(".jar")) {
      jar.getParentFile
    } else {
      jar.getParentFile.getParentFile.getParentFile //sbt
    }
  }

  def main(args: Array[String]): Unit = {
    args.toList match {
      case Nil => println("command not specified")
      case "import" :: "all" :: Nil=> {
        val database = Database.create(delete = true, getWorkingDirectory) // or true??
        database.downloadAndImportAll(getWorkingDirectory)
        database.shutdown()
      }

      case "import" :: "uniprot" :: Nil => {
        val uniprotFile = new File(getWorkingDirectory, "uniprot_sprot_human.dat.gz")
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importUniprot(uniprotFile)
        database.shutdown()
      }

      case "import" :: "uniprot" :: path :: Nil => {
        val uniprotFile = new File(path)
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importUniprot(uniprotFile)
        database.shutdown()
      }

      case "import" :: "hg19" :: Nil => {
        val ncbi105File = new File(getWorkingDirectory, "seq_gene_105.md.gz")
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importHG19(ncbi105File)
        database.shutdown()
      }

      case "import" :: "hg19" :: path :: Nil => {
        val ncbi105File = new File(path)
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importHG19(ncbi105File)
        database.shutdown()
      }

      case "import" :: "hg38" :: Nil => {
        val ncbi106File = new File(getWorkingDirectory, "seq_gene_106.md.gz")
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importHG38(ncbi106File)
        database.shutdown()
      }

      case "import" :: "hg38" :: path :: Nil => {
        val ncbi106File = new File(path)
        val database = Database.create(delete = false, getWorkingDirectory)
        database.importHG38(ncbi106File)
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
        if(database.isUniprotImported) {
          println("UniprotKB")
        }
        if(database.isHG19Imported) {
          println("hg19 positions")
        }
        if(database.isHG38Imported) {
          println("hg38 positions")
        }
        if(database.isGeneSetDBImported) {
          println("GeneSetDB")
        }
        if(database.isBioSystemsImported) {
          println("BioSystems")
        }
        database.shutdown()
      }

      case "download" :: "uniprot" :: Nil => {
        val uniprotFile = new File(getWorkingDirectory, "uniprot_sprot_human.dat.gz")
        Download.downloadUniprot(uniprotFile)
      }

      case "download" :: "hg19" :: Nil => {
        val ncbi105File = new File(getWorkingDirectory, "seq_gene_105.md.gz")
        Download.downloadHG19(ncbi105File)
      }

      case "download" :: "hg38" :: Nil => {
        val ncbi106File = new File(getWorkingDirectory, "seq_gene_106.md.gz")
        Download.downloadHG38(ncbi106File)
      }

      case "download" :: "GeneSetDB" :: Nil => {
        val file = new File(getWorkingDirectory, "download-gmt_h.txt")
        Download.downloadGeneSetDB(file)
      }

      case "database" :: "reset" :: Nil => {
        val database = Database.create(delete = true, getWorkingDirectory)
        database.shutdown()
      }

      case "cluster" :: "BioSystems" :: "GeneSetDB" :: d0 :: minSize :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt)
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt)
        val union = bioSystems ++= geneSetDB
        val res = GeneSetClustering.cluster(union, d0.toDouble)
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }

      case "cluster" :: "BioSystems" :: d0 :: minSize :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val bioSystems = BioSystems.getGeneSets(database.graph, minSize.toInt)
        val res = GeneSetClustering.cluster(bioSystems, d0.toDouble)
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }

      case "cluster" :: "GeneSetDB" :: d0 :: minSize :: out :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val geneSetDB = GeneSetDB.getGeneSets(database.graph, minSize.toInt)
        val res = GeneSetClustering.cluster(geneSetDB, d0.toDouble)
        GeneSetClustering.saveGeneSets(res, new File(out))
        database.shutdown()
      }



      case "gene" :: "sets" :: "GeneSetDB" :: output :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val out = new PrintWriter(new File(output))

        var counter = 0L

//        println(TitanGeneSet.getGeneSet(database.graph, "MPO_absent_tail", GeneSetDB))
//        val list = database.graph.query()
//          .has("label", TitanGeneSet.geneSetLabel)
//          .has(TitanGeneSet.geneSetDatabase, GeneSetDB.name)
//          .has(TitanGeneSet.geneSetName, "MPO_absent_tail")
//          .limit(1)
//          .vertices().iterator().toList
//
//        println(list)
        database.graph.query()
          .has("label", TitanGeneSet.geneSetLabel)
          .has(TitanGeneSet.geneSetDatabase, GeneSetDB.name)
          .vertices().iterator().foreach { geneSetVertex =>
        //  println("b")


          if(counter % 100 == 0) {
            println(counter + " gene sets processed")
          }
          counter += 1

          val geneSet = new TitanGeneSet(database.graph, geneSetVertex.asInstanceOf[TitanVertex])
          val geneSetName = geneSet.name
          val genes = geneSet.geneSet()

          for(geneID <- genes) {
            out.println(geneSetName + "\t" + geneID + "\t" + 100)
          }
        }

        out.close()
        database.shutdown()


      }

      case "gene" :: "sets" :: "BioSystems" :: output :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)

        val out = new PrintWriter(new File(output))

        var counter = 0L


        database.graph.query()
          .has("label", TitanGeneSet.geneSetLabel)
          .has(TitanGeneSet.geneSetDatabase, BioSystems.name)
          .vertices().iterator().foreach { geneSetVertex =>
          //  println("b")


          if(counter % 100 == 0) {
            println(counter + " gene sets processed")
          }
          counter += 1

          val geneSet = new TitanGeneSet(database.graph, geneSetVertex.asInstanceOf[TitanVertex])
          val geneSetName = geneSet.name
          val genes = geneSet.geneSet()

        //  println(geneSet.vertex.query().edges().toList)

         // println(geneSetName)
          for(geneID <- genes) {
            out.println(geneSetName + "\t" + geneID + "\t" + 100)
          }
        }

        out.close()
        database.shutdown()


      }

      case "compare" :: "BioSystems" :: "GeneSetDB" :: d0 :: minSize :: unique :: common :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val d0d = d0.toDouble
        val minSizeI = minSize.toInt
        val bioSystems = BioSystems.getGeneSets(database.graph, 10)
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, 10)
        GeneSetClustering.compare(bioSystems, geneSetDB, d0d, new File(unique), new File(common))
        database.shutdown()
      }

      case "compare" :: "GeneSetDB" :: "BioSystems" :: d0 :: minSize :: unique :: common :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val d0d = d0.toDouble
        val minSizeI = minSize.toInt
        val bioSystems = BioSystems.getGeneSets(database.graph, 10)
        val geneSetDB = GeneSetDB.getGeneSets(database.graph, 10)
        GeneSetClustering.compare(geneSetDB, bioSystems, d0d, new File(unique), new File(common))
        database.shutdown()
      }

      case "path" :: Nil => {
        println(getWorkingDirectory)
      }

      case _ => println("unknown command")
    }
  }
}
