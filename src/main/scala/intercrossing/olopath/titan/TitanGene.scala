package intercrossing.olopath.titan

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Vertex
import intercrossing.olopath.Gene

import scala.collection.JavaConversions._

object TitanGene {
  val geneLabel = "GENE"
//  val startPositionProperty = "START"
//  val endPositionProperty = "END"
  val uniprotID = "GENE_UNIPROT"
  val geneSymbolProperty = "GENE_SYMBOL"
  val geneIDProperty = "GENEID"
  val geneMIMProperty = "GENE_MIM"


  def byGeneID(graph: TitanGraph, geneID: Long): Option[TitanGene] = {
    val it = graph.query()
      .has("label", geneLabel)
      .has(geneIDProperty, geneID)
      .vertices().iterator()

    if(it.hasNext) {
      Some(new TitanGene(graph, it.next()))
    } else {
      None
    }

  }
}

//or locus???
class TitanGene(graph: TitanGraph, val vertex: Vertex) extends Gene {

  override def symbol: String = {
    vertex.getProperty(TitanGene.geneSymbolProperty)
  }

  def mim: String = {
    vertex.getProperty(TitanGene.geneMIMProperty)
  }

  def geneIDs: List[java.lang.Long] = {
    val raw: java.util.ArrayList[java.lang.Long] = vertex.getProperty(TitanGene.geneIDProperty)
    raw.toList
  }

  def uniprotId: String = {
    vertex.getProperty(TitanGene.uniprotID)
  }

//  override def names: Set[String] = {
//    val names: java.util.ArrayList[String] = vertex.getProperty(TitanGene.geneNameProperty)
//    names.toSet
//  }
//
//  override def end: Long = {
//    vertex.getProperty(TitanGene.endPositionProperty)
//  }
//
//  override def start: Long = {
//    vertex.getProperty(TitanGene.startPositionProperty)
//
//  }

  override def toString: String = {
    //"Gene(" + start + "," + end + "," + names + ")"
    "Gene(" + symbol + ", GeneID:" + geneIDs + ", " + uniprotId + ", " + mim + ")"
  }
}
