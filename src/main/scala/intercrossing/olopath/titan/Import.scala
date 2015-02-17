package intercrossing.olopath.titan

import java.io.{FileInputStream, File}
import java.util.zip.GZIPInputStream
import intercrossing.olopath.CLI

import scala.collection.JavaConversions._

import com.thinkaurelius.titan.core.TitanGraph

import scala.collection.mutable

object Import {


  def ncbiPositionsImport(seqGeneFile: File, assembly: String, graph: TitanGraph, reference: TitanReference): Unit = {
    //9606	12	126927027	126957331	+	NT_009755.19	4346404	4376708	+	LOC100128554	GeneID:100128554	GENE	GRCh37.p13-Primary Assembly	-
    val seqGeneRegexp = "9606\\s+([^\\s]+)\\s+(\\d+)\\s+(\\d+).+GeneID:(\\d+)\\s+GENE\\s+$assembly$.+"
      .replace("$assembly$", assembly)
      .r
    //"GRCh37\\.p13"
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(seqGeneFile)))

    val chromosomes = new mutable.HashSet[String]()
    var counter = 0
    source.getLines().foreach {
      case seqGeneRegexp(chr, start, end, geneID) => {
      //  println(chr + ":" + start + "-" + end + " " + geneID)
        val chName = chr.split('|')(0)
        chromosomes += chName

        val chromosome = reference.getOrCreateChromosome(chName)

        graph.query()
          .has("label", TitanGene.geneLabel)
          .has(TitanGene.geneIDProperty, geneID).vertices().foreach { geneVertex =>
          chromosome.addPosition(start.toLong, end.toLong, geneVertex)
        }

        if(counter % 500 == 0) {
          println(counter + " processed")
        }
        counter += 1
      }
      case line => {
        if(line.contains("GENE") && line.contains("GRCh3")) {
          println("error unexpected line: " + line)
        }
      }
    }
    graph.commit()
    println(counter)
    println(chromosomes)
  }


  def addGene(graph: TitanGraph, uniprotId: String, geneIDs: List[String], geneSymbol: String, mimId: String): Unit = {
   // println("addGene(" + uniprotId + "," + geneID + "," + geneSymbol + ")")

    if(uniprotId.isEmpty) {
     // println("error: uniprotId is empty GeneID:" + geneID + " Symbol:" + geneSymbol)
    }

    val geneIdsLong: List[Long] = geneIDs.flatMap { id =>
      if (id.matches("\\d+")) {
        Some(id.toLong)
      } else {
        None
      }
    }

   // if (geneID.isEmpty) {
      //println("error: GeneID is empty Id:" + uniprotId + " Symbol:" + geneSymbol)
   // }

    if (geneSymbol.isEmpty) {
      //println("warning: Gene Symbol is empty Id:" + uniprotId + " GeneID:" + geneID)
    }

    //graph.indexQuery("by" + TitanGene.geneIDProperty, geneID).vertices().iterator().toList match {
    graph.query().has("label", TitanGene.geneLabel).has(TitanGene.uniprotID, uniprotId).vertices().iterator().toList match {

    case Nil => {
        //should create new gene
        val vertex = graph.addVertexWithLabel(TitanGene.geneLabel)
        vertex.setProperty(TitanGene.uniprotID, uniprotId)
        vertex.setProperty(TitanGene.geneSymbolProperty, geneSymbol)

      geneIdsLong.foreach { geneId =>
//        if(geneId == 333932) {
//          println("333932")
//        }
        vertex.addProperty(TitanGene.geneIDProperty, geneId)
      }

    if(!mimId.isEmpty) {
      vertex.addProperty(TitanGene.geneMIMProperty, mimId)
    }

     }      case oneVertex :: Nil => {
      println("uniprot id is not unique for " + uniprotId)
    }
//        //two uniprot entry with the same geneid!!!
//       // println("two uniprot entry with the same geneid: " + geneID + " uniprotid: " + uniprotId)
//        //concatinate
//      }
     case _ => {
        println("uniprot id is not unique for " + uniprotId)
//
//        //  println("more than uniprot entry with the same geneid: " + geneID + " uniprotid: " + uniprotId)
     }
    }
  }


  def importUniprot(uniprotFile: File, graph: TitanGraph): Unit = {

    println("importing Uniprot from " + uniprotFile.getName)
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(uniprotFile)))
    var geneSymbol = ""
    var uniprotId = ""
    var mim = ""
    var geneIDs = new mutable.ArrayBuffer[String]()

    var counter = 0

    //todo implement several
    val idRegexp = """ID\s+(\w+)\s+.+""".r

    //DR   MIM; 608579; phenotype.
    val mimRegexp = """DR\s+MIM;\s+(\d+);\s+phenotype.+""".r

    //DR   GeneID; 7529; -.
    //DR   GeneID; 200316; -.
    val geneIdRegexp = """DR\s+GeneID;\s+(\d+);.+""".r

    //GN   Name=HLA-A; Synonyms=HLAA;
    val geneSymbolRegexp = """GN\s+Name=([^;\s]+).+""".r

    var first = true
    //val
    source.getLines().foreach {
      case idRegexp(rid) => {
       // println("UniprotId: " + rid)

        if(counter % 1000 == 0) {
          println(counter + " processed")
        //  graph.commit()
        }

        if(first) {
          first = false
        } else {




          addGene(graph, uniprotId, geneIDs.toList, geneSymbol, mimId = mim)
          geneSymbol = ""
          //uniprotName = ""
          mim = ""
          geneIDs.clear()
        }
        //add gene

        uniprotId = rid
      //  println(rid + "-" + first)

        counter += 1


      }

      case geneSymbolRegexp(symbol) => {
      // println("Gene Symbol: " + symbol)

        geneSymbol = symbol
      }

      case mimRegexp(mimId) => {
        mim = mimId
      }
      case geneIdRegexp(id) => {
     //  println("GeneID: " + id)
        geneIDs += id
      }
      case _ => ()
    }
    addGene(graph, uniprotId, geneIDs.toList, geneSymbol, mimId = mim)
    graph.commit()

    //val hlas =graph.query()
    //  .has("label", TitanGene.geneLabel)
    //  .has(TitanGene.geneSymbolProperty, "HLA-A").vertices().toList.map(new TitanGene(graph, _))

   // println(hlas)
   // println(hlas.size)

   // println(counter)



  }

  def importGenSetDB(file: File, graph: TitanGraph): Unit = {
    println("importing GeneSetDB from " + file.getName)
    var counter = 0L
    val source = io.Source.fromFile(file).getLines().foreach { line =>
      val parts = line.split("\\s+")
      if(parts.size < 3) {
        println("parse error: " + line)
      } else {
        val setName = parts(0)
        val dataSourse = parts(1)
        val geneIds = parts.drop(2).flatMap { id =>
          if(id.matches("\\d+")) {
            Some(id.toLong)
          } else {
            None
          }
        }

        if (!dataSourse.startsWith("GO")) {

          val geneSet = TitanGeneSet.getOrCreateGeneSet(graph, setName, GeneSetDB, dataSourse)
          geneSet.addGenes(geneIds)

          if (counter % 100 == 0) {
            println(counter + " gene sets processed")
            if (counter % 100 == 0) {
              graph.commit()
            }
          }
          counter += 1
        }
      }
    }
    graph.commit()
    println(counter + " gene sets added")
  }
}
