package intercrossing.olopath.titan

import scala.collection.JavaConversions._

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.{Direction, Vertex}
import intercrossing.olopath.{Chromosome, Reference}

object TitanReference {
  val referenceLabel = "REFERENCE"
  val nameProperty = "REFERENCE_NAME"

  def createReference(graph: TitanGraph, id: String): TitanReference = {
    val reference = graph.addVertexWithLabel(referenceLabel)
    reference.addProperty(nameProperty, id)
    new TitanReference(graph, id, reference)
  }

  def getOrCreateReference(graph: TitanGraph, id: String): TitanReference = {
    apply(graph, id) match {
      case Some(ref) => ref
      case None => createReference(graph, id)
    }
  }

  def apply(titan: TitanGraph, id: String): Option[TitanReference] = {
    val vertex = titan.query().has("label", referenceLabel).has(TitanReference.nameProperty, id).limit(1).vertices().headOption
    vertex.map(new TitanReference(titan, id, _))
  }
}
class TitanReference(graph: TitanGraph, id: String, val vertex: Vertex) extends Reference {

  def getChromosome(name: String): Option[TitanChromosome] = {
    val iterator = vertex.query()
      .labels(TitanChromosome.edgeLabel)
      .direction(Direction.OUT)
      .has(TitanChromosome.nameEdgeProperty, name)
      .vertices().iterator()

    if(iterator.hasNext) {
      Some(new TitanChromosome(graph, iterator.next()))
    } else {
      None
    }
  }

  def getOrCreateChromosome(name: String): TitanChromosome = {
    val iterator = vertex.query()
      .labels(TitanChromosome.edgeLabel)
      .direction(Direction.OUT)
      .has(TitanChromosome.nameEdgeProperty, name)
      .vertices().iterator()

    if(iterator.hasNext) {
      new TitanChromosome(graph, iterator.next())
    } else {
      createChromosome(name)
    }
  }

  override def getChromosomes: List[Chromosome] = {
    vertex.query()
      .labels(TitanChromosome.edgeLabel)
      .direction(Direction.OUT).vertices().toList
      .map(new TitanChromosome(graph, _))
  }

  def createChromosome(name: String): TitanChromosome = {
    val chromosomeVertex = graph.addVertex()
    chromosomeVertex.setProperty(TitanChromosome.nameVertexProperty, name)
    val chromosomeEdge = vertex.addEdge(TitanChromosome.edgeLabel, chromosomeVertex)
    chromosomeEdge.setProperty(TitanChromosome.nameEdgeProperty, name)
    new TitanChromosome(graph, chromosomeVertex)
    graph.commit()
    new TitanChromosome(graph, chromosomeVertex)
  }
}
