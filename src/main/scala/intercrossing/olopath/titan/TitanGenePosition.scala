package intercrossing.olopath.titan

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.{Direction, Edge}

object TitanGenePosition {
  val genePositionLabel = "GENE_POSITION"
  val startProperty = "GENE_START"
  val endProperty = "GENE_END"
}

class TitanGenePosition(graph: TitanGraph, edge: Edge) {
  def gene: TitanGene = {
    new TitanGene(graph, edge.getVertex(Direction.IN))
  }

  def start: Long = {
    edge.getProperty(TitanGenePosition.startProperty)
  }

  def end: Long = {
    edge.getProperty(TitanGenePosition.endProperty)
  }

  def chromosome: TitanChromosome = {
    new TitanChromosome(graph, edge.getVertex(Direction.OUT))
  }

  override def toString: String = {
    chromosome + ":" + start + "-" + end + " " + gene
  }
}
