package intercrossing.olopath.titan

import java.io.{FileInputStream, File}
import java.util.zip.GZIPInputStream

import com.thinkaurelius.titan.core.TitanGraph

import scala.collection.mutable

object BioSystemsImport {

  def importBioSystemsTaxonomy(graph: TitanGraph, taxonomyFile: File) = {
    println("importing BioSystems taxonomy from " + taxonomyFile)
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(taxonomyFile)))

    val specific = new mutable.HashSet[Long]()

    source.getLines().foreach { line =>
      val fields = line.split("\\s+")
      val bsID = fields(0).toLong
      val taxId = fields(1).toLong

      specific += bsID
      if (taxId == 9606) {
        TitanGeneSet.getOrCreateGeneSetBioSystems(graph, bsID.toString, taxId)
      }
    }
  }

  def genesImport(graph: TitanGraph, genesFile: File) {
    println("importing BioSystems from " + genesFile.getName)
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(genesFile)))

    var lineCounter = 0L
    var genesAdded = 0L
    var genesAdded2 = 0L

    source.getLines().foreach { line =>
      if (lineCounter % 100000 == 0) {
        graph.commit()
        println(lineCounter + " processed")

      }
      lineCounter += 1
      val fields = line.split("\\s+")
      val bsID = fields(0).toLong
      val geneID = fields(1).toLong

      TitanGeneSet.getGeneSet(graph, bsID.toString, BioSystems) match {
        case None => {
          //not human specific
        }
        case Some(geneSet) => {
          genesAdded += 1
          genesAdded2 += geneSet.addGene(geneID)
        }
      }
    }
    println("genes added: " + genesAdded)
  }

  def bioSystemsLikeGenesImport(database: GeneSetDatabase, graph: TitanGraph, geneSets: File): Unit = {
    println("importing gene sets from from " + geneSets.getName)

    val source = if(geneSets.getName.endsWith(".gz")) {
      io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(geneSets)))
    } else {
      io.Source.fromInputStream(new FileInputStream(geneSets))
    }

    var lineCounter = 0L
    var genesAdded = 0L
    var genesAdded2 = 0L

    source.getLines().foreach { line =>
      if (lineCounter % 100000 == 0) {
        graph.commit()
        println(lineCounter + " processed")

      }
      lineCounter += 1
      val fields = line.split("\\s+")
      val bsID = fields(0).toLong
      val geneID = fields(1).toLong


      val geneSet = TitanGeneSet.getOrCreateGeneSet(graph, bsID.toString, database, database.name)
    //  println("adding gene id " + geneID + " to " + geneSet.name)
      genesAdded += 1
      genesAdded2 += geneSet.addGene(geneID)

    }
    println("genes added: " + genesAdded)
  }


}
