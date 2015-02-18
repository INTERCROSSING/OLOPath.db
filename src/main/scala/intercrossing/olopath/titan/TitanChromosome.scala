package intercrossing.olopath.titan

import scala.collection.JavaConversions._
import com.thinkaurelius.titan.core.{TitanGraph}
import com.tinkerpop.blueprints.{Compare, Direction, Vertex}
import intercrossing.olopath.{Gene, Chromosome}

object TitanChromosome {
  val edgeLabel = "CHROMOSOME"
  val nameEdgeProperty = "CHR_NAME"
  val nameVertexProperty = "CHR_NAME_V"
}

class TitanChromosome(graph: TitanGraph, val vertex: Vertex) extends Chromosome {
  override def name: String = vertex.getProperty(TitanChromosome.nameVertexProperty)


  override def toString: String = {
    "ch" + name
  }

  def getGenes: List[Gene] = {
    vertex.query()
      .labels(TitanGene.geneLabel)
      .direction(Direction.OUT).vertices().toList
      .map(new TitanGene(graph, _))
  }

  def findNearestStart(start: Long): Option[TitanGene] = {
    val edge = vertex.query()
      .labels(TitanGenePosition.genePositionLabel)
      .direction(Direction.OUT)
      .has(TitanGenePosition.startProperty, Compare.LESS_THAN_EQUAL, start)
      .limit(1)
      .edges().headOption
    edge.map { e =>
      val pos = new TitanGenePosition(graph, e)
      println("found position: " + pos)
      pos.gene
    }

  }

  def addPosition(start: Long, end: Long, gene: Vertex): Unit = {
    vertex.query()
      .labels(TitanGenePosition.genePositionLabel)
      .direction(Direction.OUT)
      .has(TitanGenePosition.startProperty, start)
      .edges().headOption match {
      case None => {
        val positionEdge = vertex.addEdge(TitanGenePosition.genePositionLabel, gene)
        positionEdge.setProperty(TitanGenePosition.startProperty, start)
        positionEdge.setProperty(TitanGenePosition.endProperty, end)
      }
      case Some(pos) => {
        //println("already added")
      }
    }
  }

}
