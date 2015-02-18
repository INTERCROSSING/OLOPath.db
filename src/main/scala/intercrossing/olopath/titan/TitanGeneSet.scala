package intercrossing.olopath.titan

import com.thinkaurelius.titan.core.{TitanVertex, Order, TitanGraph}
import com.tinkerpop.blueprints.{Direction}
import scala.collection.JavaConversions._
import scala.collection.mutable

object GeneSetDatabase {

  val geneSetDBLabel = "GENE_SET_DATABASE_LABEL"
  val geneSetDBName = "GENE_SET_DATABASE_NAME"
  val geneSetDBTOGeneSetEDGE = "DB_TO_GENE_SET_LABEL"
  val geneSetDBTOGeneSetNAME = "DB_TO_GENE_SET_NAME"

  def apply(s: String) = s match {
    case "GeneSetDB" => GeneSetDB
    case "BioSystems" => BioSystems
  }

}

sealed trait GeneSetDatabase {

  def name: String

  def getGeneSets(graph: TitanGraph, minSize: Int, maxSize: Option[Int] = None): mutable.HashMap[String, mutable.ArrayBuffer[Long]] = {

    println("loading " + name)
    var geneSets = 0L
    var filtered = 0L
    val res = new mutable.HashMap[String, mutable.ArrayBuffer[Long]]
    graph.query()
      .has("label", TitanGeneSet.geneSetLabel)
      .has(TitanGeneSet.geneSetDatabase, name)
      .vertices().iterator().foreach { geneSetVertex =>

      val geneSet = new TitanGeneSet(graph, geneSetVertex.asInstanceOf[TitanVertex])
      val geneSetName = geneSet.name
      val genes = geneSet.geneIDList()

      if (genes.length > minSize && maxSize.forall(_ > genes.size)) {
        res.put(geneSetName, genes)
        geneSets += 1

        if (geneSets % 500 == 0) {
          println(geneSets + " loaded...")
        }
      } else {
        filtered += 1
      }
    }

    println("loaded: " + geneSets + " filtered: " + filtered)
    res
  }
}

case object GeneSetDB extends GeneSetDatabase {
  def name = "GeneSetDB"


}

case object BioSystems extends GeneSetDatabase {
  def name = "BioSystems"
}

object TitanGeneSet {
  val geneSetLabel = "GENE_SET_LABEL"
  val geneSetDatabase = "GENE_SET_DATABASE"
  val geneSetSource = "GENE_SET_SRC"
  val geneSetName = "GENE_SET_NAME"
  val geneSetTaxId = "GENE_SET_TAX"

  val geneSetToGeneEdgeGeneId = "GENE_SET_EDGE_GENE_ID"
  val geneSetToGeneEdgeLabel = "GENE_SET_EDGE_LABEL"


  def getGeneSet(graph: TitanGraph, name: String, database: GeneSetDatabase): Option[TitanGeneSet] = {
    val it = graph.query()
      .has("label", geneSetLabel)
      .has(geneSetName, name)
      .has(geneSetDatabase, database.name)
      .vertices().iterator()

    if (it.hasNext) {
      Some(new TitanGeneSet(graph, it.next().asInstanceOf[TitanVertex]))
    } else {
      None
    }
  }

  def createGeneSet(graph: TitanGraph, name: String, database: GeneSetDatabase, source: String): TitanGeneSet = {
    val vertex = graph.addVertexWithLabel(geneSetLabel)
    vertex.setProperty(geneSetName, name)
    vertex.setProperty(geneSetDatabase, database.name)
    vertex.setProperty(geneSetSource, source)
    new TitanGeneSet(graph, vertex)
  }

  def createGeneSetBioSystems(graph: TitanGraph, name: String, taxId: Long): TitanGeneSet = {
    val vertex = graph.addVertexWithLabel(geneSetLabel)
    vertex.setProperty(geneSetName, name)
    vertex.setProperty(geneSetDatabase, BioSystems.name)
    vertex.setProperty(geneSetSource, "BioSystems")
    vertex.setProperty(geneSetTaxId, taxId)
    new TitanGeneSet(graph, vertex)
  }

  def getOrCreateGeneSetBioSystems(graph: TitanGraph, name: String, taxId: Long): TitanGeneSet = {
    getGeneSet(graph, name, BioSystems) match {
      case None => createGeneSetBioSystems(graph, name, taxId)
      case Some(set) => set
    }
  }

  def getOrCreateGeneSet(graph: TitanGraph, name: String, database: GeneSetDatabase, source: String): TitanGeneSet =
    getGeneSet(graph, name, database) match {
      case None => createGeneSet(graph, name, database, source)
      case Some(set) => set
    }
}

class TitanGeneSet(graph: TitanGraph, val vertex: TitanVertex) {

  import TitanGeneSet._

  def database: GeneSetDatabase = {
    GeneSetDatabase(vertex.getProperty(geneSetDatabase))
  }

  def name: String = vertex.getProperty(geneSetName)

  def source = vertex.getProperty(geneSetSource)

  def getGenes(): List[TitanGene] = {
    vertex.getVertices(Direction.OUT, geneSetToGeneEdgeLabel).iterator().toList.map { vertex =>
      new TitanGene(graph, vertex)
    }
  }

  def geneSet(): mutable.HashSet[Long] = {
    val res = new mutable.HashSet[Long]()
    vertex
      .query()
      .labels(geneSetToGeneEdgeLabel)
      .edges().iterator().foreach { edge =>
      res += edge.getProperty(geneSetToGeneEdgeGeneId)
    }
    res
  }

  def geneIDList(): mutable.ArrayBuffer[Long] = {
    val res = new mutable.ArrayBuffer[Long]()
    vertex
      .query()
      .labels(geneSetToGeneEdgeLabel)
      .orderBy(geneSetToGeneEdgeGeneId, Order.ASC)
      .edges()
      .iterator().foreach { edge =>
      res += edge.getProperty(geneSetToGeneEdgeGeneId)
    }
    res
  }


  def addGene(geneID: Long): Int = {
    var res = 0
    val it = vertex.query()
      .labels(TitanGeneSet.geneSetToGeneEdgeLabel)
      .direction(Direction.OUT)
      .has(TitanGeneSet.geneSetToGeneEdgeGeneId, geneID).edges().iterator()

    if (!it.hasNext) {
      TitanGene.byGeneID(graph, geneID).foreach { gene =>
        val edge = vertex.addEdge(geneSetToGeneEdgeLabel, gene.vertex)
        edge.setProperty(geneSetToGeneEdgeGeneId, geneID)
        res += 1
      }
      if (res == 0) {
        // println(geneID + " not found")
      }
    } else {
      // println(geneID + "already added")
    }
    res
  }

  def addGenes(geneIDs: Traversable[Long]): Unit = {
    val addedGeneIds = geneSet()
    geneIDs.foreach { geneID =>
      if (!addedGeneIds.contains(geneID)) {
        addGene(geneID)
      }
    }
  }
}
