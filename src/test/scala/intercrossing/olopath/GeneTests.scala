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
    if(!database.isModuleImported(UniprotKBModule)) {
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
    if(!database.isModuleImported(UniprotKBModule)) {
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
  def uniprotACTest(): Unit = {
    if(!database.isModuleImported(UniprotKBModule)) {
      println("error: uniprot must be imported for this test")
    } else {
      val id: Long = 79989
      val gene = TitanGene.byGeneID(graph, id)

      //AC   A0AVF1; A4D1S3; B7Z5M0; C9J2N7; F8W724; Q9H9S8; Q9NTC0;
      //println(gene.map(_.uniprotACs))

      assertEquals(true, gene.exists(_.uniprotACs.contains("A0AVF1")))
      assertEquals(true, gene.exists(_.uniprotACs.contains("Q9NTC0")))

    }
  }



  @Test
  def geneSetDBSortedTest(): Unit = {
    if(!database.isModuleImported(GeneSetDatabaseModule(GeneSetDB))) {
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
    if(!database.isModuleImported(GeneSetDatabaseModule(BioSystems))) {
      println("error: BioSystems must be imported for this test")
    } else {

      val res = BioSystems.getGeneSets(graph, 0)

      assertEquals(true, res.forall { case (name, geneSet) =>
        SetUtils.isSorted(geneSet)._1
      })
    }
  }

  @Test
  def bioSystemsSortedTest2(): Unit = {
    if(!database.isModuleImported(GeneSetDatabaseModule(BioSystems))) {
      println("error: BioSystems must be imported for this test")
    } else {


      assertEquals(List[TitanGene](), TitanGeneSet.getGeneSet(graph, "920961", BioSystems).get.getGenes())
      //val res = BioSystems.getGeneSets(graph, 0)


    }
  }




  @Test
  def geneSetDBTest(): Unit = {
    if(!database.isModuleImported(GeneSetDatabaseModule(GeneSetDB))) {
      println("error: GeneSetDB must be imported for this test")
    } else {
      TitanGeneSet.getGeneSet(graph, "MPO_absent_tail", GeneSetDB) match {
        case None => assertEquals(None, Some(""))
        case Some(set) => {
          val genes = set.getGenes()
          assertEquals(14, genes.size)
         // println(genes)
          assertEquals(true, genes.exists(_.geneIDs.contains(9241)))
        }
      }
    }
  }

  @Test
  def intPathTest(): Unit = {
    if(!database.isModuleImported(GeneSetDatabaseModule(IntPath))) {
      println("error: IntPath must be imported for this test")
    } else {
      TitanGeneSet.getGeneSet(graph, "Glycolysis and Gluconeogenesis", IntPath) match {
        case None => assertEquals(None, Some(""))
        case Some(set) => {
          val genes = set.getGenes()
          assertEquals(74, genes.size) //91?
        //  println(genes)
          assertEquals(true, genes.exists(_.geneIDs.contains(5211)))
        }
      }
    }
  }

  @Test
  def hg38Position(): Unit = {
    if(!database.isModuleImported(HG38Module)) {
      println("error: hg38 must be imported for this test")
    } else {
      val hg38o = TitanReference(graph, "hg38")

      assertEquals(true, hg38o.isDefined)
      val hg38 = hg38o.head

      val ch10o = hg38.getChromosome("10")
      assertEquals(true, ch10o.isDefined)

      val ch10 = ch10o.get

      assertEquals(Some("TCF7L2"), ch10.findNearestStart(112950319).map(_.symbol))
      assertEquals(Some("125853"), ch10.findNearestStart(112950319).map(_.mim))
    }
  }

}
