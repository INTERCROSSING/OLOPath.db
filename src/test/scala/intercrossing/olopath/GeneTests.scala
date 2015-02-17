package intercrossing.olopath

import intercrossing.olopath.titan._
import org.junit.Test
import org.junit.Assert._
import scala.collection.JavaConversions._


class GeneTests {

  val database = Database.create(false, CLI.getWorkingDirectory)
  val graph = database.graph

  @Test
  def testHLA(): Unit = {
    if(!database.isUniprotImported) {
      println("error: uniprot must be imported for this test")

    } else {
      val hlas = graph.query()
        .has("label", TitanGene.geneLabel)
        .has(TitanGene.geneSymbolProperty, "HLA-A").vertices().toList.map(new TitanGene(graph, _))

      assertEquals(21, hlas.size)
    }

  }

  @Test
  def geneIDTest(): Unit = {
    if(!database.isUniprotImported) {
      println("error: uniprot must be imported for this test")
    } else {
      val id: Long = 204219
      val gene = TitanGene.byGeneID(graph, id)
      assertEquals(true, gene.exists(_.geneIDs.contains(id)))


      val id2: Long = 333932
      val gene2 = TitanGene.byGeneID(graph, id2)
      assertEquals(true, gene2.exists(_.geneIDs.contains(id2)))

    }

  }

  @Test
  def geneSetDBSortedTest(): Unit = {
    if (!database.isGeneSetDBImported) {
      println("error: GeneSetDB must be imported for this test")
    } else {

      val res = GeneSetDB.getGeneSets(graph, 0)

      assertEquals(true, res.forall { case (name, geneSet) =>
        SetUtils.isSorted(geneSet)._1
      })
    }
  }

  @Test
  def bioSystemsSortedTest(): Unit = {
    if (!database.isBioSystemsImported) {
      println("error: BioSystems must be imported for this test")
    } else {

      val res = BioSystems.getGeneSets(graph, 0)

      assertEquals(true, res.forall { case (name, geneSet) =>
        SetUtils.isSorted(geneSet)._1
      })
    }
  }


  @Test
  def geneSetDBTest(): Unit = {
    if(!database.isGeneSetDBImported) {
      println("error: GeneSetDB must be imported for this test")
    } else {
      TitanGeneSet.getGeneSet(graph, "MPO_absent_tail", GeneSetDB) match {
        case None => assertEquals(None, Some(""))
        case Some(set) => {
          val genes = set.getGenes()
          assertEquals(14, genes.size)
          println(genes)
          assertEquals(true, genes.exists(_.geneIDs.contains(9241)))
        }
      }
    }
  }

  @Test
  def hg38Position(): Unit = {
    if(!database.isHG38Imported) {
      println("error: hg38 must be imported for this test")
    } else {
      val hg38o = TitanReference(graph, "hg38")

      assertEquals(true, hg38o.isDefined)
      val hg38 = hg38o.head

      val ch10o = hg38.getChromosome("10")
      assertEquals(true, ch10o.isDefined)

      val ch10 = ch10o.get


      //112950219 .. 113167678
      assertEquals(Some("TCF7L2"), ch10.findNearestStart(112950319).map(_.symbol))
      assertEquals(Some("125853"), ch10.findNearestStart(112950319).map(_.mim))
    }
  }

  @Test
  def rawVertexTest(): Unit = {
    import TitanGene._
    import ohnosequences.olopath.titan.TitanChromosome._
    //val hg38 = TitanReference(graph, "hg38").get
    //hg38.vertex.query().has(TitanChromosome.)

    //chrom


  }

}
