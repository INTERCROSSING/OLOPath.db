package intercrossing.olopath.titan

import java.io.{FileInputStream, File}
import java.util.zip.GZIPInputStream

import com.thinkaurelius.titan.core.TitanGraph

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object BioSystemsImport {


  def importBioSystemsTaxonomy(graph: TitanGraph, taxonomyFile: File) = {
    println("importing BioSystems taxonomy from " + taxonomyFile)
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(taxonomyFile)))

    val humanSpecific = new mutable.HashSet[Long]()
    val specific = new mutable.HashSet[Long]()

    source.getLines().foreach { line =>
      val fields = line.split("\\s+")
      val bsID = fields(0).toLong
      val taxId = fields(1).toLong

     

      specific += bsID
      if(taxId == 9606) {
        TitanGeneSet.getOrCreateGeneSetBioSystems(graph, bsID.toString, taxId)
       // humanSpecific += bsID
      }
    }

   // println("human specific: " + humanSpecific.size)
    //println("organism specific: " + specific.size)
   // (humanSpecific, specific)
  }

  def genesImport(graph: TitanGraph, genesFile: File): Unit = {

    println("importing BioSystems from " + genesFile.getName)
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(genesFile)))

   // val geneSets = new mutable.HashMap[Long, ArrayBuffer[Long]]()

    var lineCounter = 0L

    var genesAdded = 0L
    var genesAdded2 = 0L


    source.getLines().foreach { line =>

      if(lineCounter % 100000 == 0) {
       // println("genes added: " + genesAdded)
       // println("genes added: " + genesAdded2)
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
          genesAdded2 +=  geneSet.addGene(geneID)
        }
      }
//      geneSets.get(bsID) match {
//        case Some(list) => list += geneID
//        case None => {
//          val list = new ArrayBuffer[Long]()
//          list += geneID
//          geneSets.put(bsID, list)
//        }
//      }
    }

  //  println(geneSets.size + " gene sets")

//    var human = 0L
//    var orgSpecific = 0L
//    var conserved = 0L

//    geneSets.foreach { case (bsid, list) =>
//
//        if(humanSpecific.contains(bsid)) {
//          human += 1
//        }
//        if(specific.contains(bsid)) {
//          orgSpecific += 1
//        } else {
//          conserved += 1
//        }
//    }

//    println("humanSpecific: " + human)
    println("genes added: " + genesAdded)
//    println("gene: " + conserved)

//    val list = geneSets(424)
//    if (!isSorted(list)._1) {
//       println("is not sorted: " + isSorted(list)._2)
//    }


//    geneSets.foreach { case (bsid, list) =>
//      //println(bsid + ": " + list)
//      if (!isSorted(list)) {
//        println(bsid + ": " + "is not sorted")
//      }
//    }

  }


}
