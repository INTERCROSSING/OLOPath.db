package intercrossing.olopath.titan

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Vertex
import intercrossing.olopath.Gene

import scala.collection.JavaConversions._

object TitanGene {
  val geneLabel = "GENE"
  val geneUniprotIDProperty = "GENE_UNIPROT_ID"
  val geneSymbolProperty = "GENE_SYMBOL"
  val geneIDProperty = "GENE_ID"
  val geneUniprotACProperty = "GENE_UNIPROT_AC"
  val geneMIMProperty = "GENE_MIM"


  def byGeneID(graph: TitanGraph, geneID: Long): Option[TitanGene] = {
    val it = graph.query()
      .has("label", geneLabel)
      .has(geneIDProperty, geneID)
      .vertices().iterator()

    if (it.hasNext) {
      Some(new TitanGene(graph, it.next()))
    } else {
      None
    }

  }

  def byGeneSymbol(graph: TitanGraph, geneSymbol: String): Option[TitanGene] = {
    val it = graph.query()
      .has("label", geneLabel)
      .has(geneSymbolProperty, geneSymbol)
      .vertices().iterator()

    if (it.hasNext) {
      Some(new TitanGene(graph, it.next()))
    } else {
      None
    }

  }

  def byUniprotAC(graph: TitanGraph, ac: String): Option[TitanGene] = {
    val it = graph.query()
      .has("label", geneLabel)
      .has(geneUniprotACProperty, ac)
      .vertices().iterator()

    if (it.hasNext) {
      Some(new TitanGene(graph, it.next()))
    } else {
      None
    }

  }
}

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

  def uniprotACs: List[String] = {
    val raw: java.util.ArrayList[String] = vertex.getProperty(TitanGene.geneUniprotACProperty)
    raw.toList
  }

  def uniprotId: String = {
    vertex.getProperty(TitanGene.geneUniprotIDProperty)
  }

  override def toString: String = {
    "Gene(" + symbol + ", GeneID:" + geneIDs + ", " + uniprotId + ", " + mim + ")"
  }
}
