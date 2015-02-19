package intercrossing.olopath.titan

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Vertex
import intercrossing.olopath.Gene

import scala.collection.JavaConversions._

object TitanGene {
  val geneLabel = "GENE"
  val uniprotID = "GENE_UNIPROT"
  val geneSymbolProperty = "GENE_SYMBOL"
  val geneIDProperty = "GENEID"
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

  def uniprotId: String = {
    vertex.getProperty(TitanGene.uniprotID)
  }

  override def toString: String = {
    "Gene(" + symbol + ", GeneID:" + geneIDs + ", " + uniprotId + ", " + mim + ")"
  }
}
