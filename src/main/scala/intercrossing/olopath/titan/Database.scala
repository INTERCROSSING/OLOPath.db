package intercrossing.olopath.titan

import java.io.File
import com.thinkaurelius.titan.core._
import com.thinkaurelius.titan.graphdb.query.TitanPredicate
import com.tinkerpop.blueprints.{Direction, Vertex}
import intercrossing.olopath._


object Database {

  def create(delete: Boolean, workingDirectory: File): Database = {
    val dbLocation = new File(workingDirectory, "database")

    if (delete) {
      org.apache.commons.io.FileUtils.deleteDirectory(dbLocation)
    }

    val g = TitanFactory.build()
      .set("storage.backend", "berkeleyje")
      .set("storage.directory", dbLocation.getAbsolutePath)
      // .set("storage.backend","cassandra")
      // .set("storage.hostname","127.0.0.1")
      .set("attributes.allow-all", false)
      .set("query.force-index", true)
      .open()

    val mgmt = g.getManagementSystem()

    if (delete) {
      val importedLabel = mgmt.makeVertexLabel("imported").make()
      val moduleName = mgmt.makePropertyKey("module").dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()

      mgmt.buildIndex("importedByModuleName", classOf[Vertex]).addKey(moduleName).indexOnly(importedLabel).buildCompositeIndex()

      val fakeName = mgmt.makePropertyKey("fake").dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("importedByFake", classOf[Vertex]).addKey(fakeName).indexOnly(importedLabel).buildCompositeIndex()

      // mgmt.buildIndex("listModules", classOf[Vertex]).indexOnly(importedLabel).()

      val referenceLabel = mgmt.makeVertexLabel(TitanReference.referenceLabel).make()
      val referenceName = mgmt.makePropertyKey(TitanReference.nameProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()

      mgmt.buildIndex("by" + TitanReference.nameProperty, classOf[Vertex]).addKey(referenceName).indexOnly(referenceLabel).buildCompositeIndex()

      val chromosomeLabel = mgmt.makeEdgeLabel(TitanChromosome.edgeLabel).multiplicity(Multiplicity.ONE2MANY).make()
      mgmt.makePropertyKey(TitanChromosome.nameVertexProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      val chromosomeName = mgmt.makePropertyKey(TitanChromosome.nameEdgeProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildEdgeIndex(chromosomeLabel, "chromosomeByName", Direction.OUT, Order.ASC, chromosomeName)


      val geneSetLabel = mgmt.makeVertexLabel(TitanGeneSet.geneSetLabel).make()
      val geneSetNameProp = mgmt.makePropertyKey(TitanGeneSet.geneSetName).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      val geneSetTaxProp = mgmt.makePropertyKey(TitanGeneSet.geneSetTaxId).dataType(classOf[java.lang.Long]).cardinality(Cardinality.SINGLE).make()

      val geneSetDBProp = mgmt.makePropertyKey(TitanGeneSet.geneSetDatabase).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()

      mgmt.buildIndex("geneSetByDB", classOf[Vertex]).indexOnly(geneSetLabel).addKey(geneSetDBProp).buildCompositeIndex()


      mgmt.buildIndex("geneSetByNameandDB", classOf[Vertex]).indexOnly(geneSetLabel).addKey(geneSetNameProp).addKey(geneSetDBProp).buildCompositeIndex()

      val geneSetToGeneLabel = mgmt.makeEdgeLabel(TitanGeneSet.geneSetToGeneEdgeLabel).multiplicity(Multiplicity.MULTI).make()

      val geneSetToGeneGeneID = mgmt.makePropertyKey(TitanGeneSet.geneSetToGeneEdgeGeneId).dataType(classOf[java.lang.Long]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildEdgeIndex(geneSetToGeneLabel, "geneSetEgeByGeneID", Direction.OUT, Order.ASC, geneSetToGeneGeneID)


      val positionLabel = mgmt.makeEdgeLabel(TitanGenePosition.genePositionLabel).multiplicity(Multiplicity.MULTI).make()
      val startPosition = mgmt.makePropertyKey(TitanGenePosition.startProperty).dataType(classOf[java.lang.Long]).cardinality(Cardinality.SINGLE).make()
      mgmt.makePropertyKey(TitanGenePosition.endProperty).dataType(classOf[java.lang.Long]).cardinality(Cardinality.SINGLE).make()


      mgmt.buildEdgeIndex(positionLabel, "geneByStart", Direction.OUT, Order.DESC, startPosition)


      val geneLabel = mgmt.makeVertexLabel(TitanGene.geneLabel).make()
      val geneIdProp = mgmt.makePropertyKey(TitanGene.geneIDProperty).dataType(classOf[java.lang.Long]).cardinality(Cardinality.LIST).make()
      mgmt.buildIndex("geneBy" + TitanGene.geneIDProperty, classOf[Vertex]).addKey(geneIdProp).indexOnly(geneLabel).buildCompositeIndex()


      val geneUniprotIdProp = mgmt.makePropertyKey(TitanGene.uniprotID).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("geneBy" + TitanGene.uniprotID, classOf[Vertex]).addKey(geneUniprotIdProp).indexOnly(geneLabel).buildCompositeIndex()

      val geneMIMProp = mgmt.makePropertyKey(TitanGene.geneMIMProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("geneBy" + TitanGene.geneMIMProperty, classOf[Vertex]).addKey(geneMIMProp).indexOnly(geneLabel).buildCompositeIndex()


      val geneSymbolPropery = mgmt.makePropertyKey(TitanGene.geneSymbolProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("geneBy" + TitanGene.geneSymbolProperty, classOf[Vertex]).addKey(geneSymbolPropery).indexOnly(geneLabel).buildCompositeIndex()

      mgmt.commit()
    }
    new Database(g)
  }
}

class Database(val graph: TitanGraph) {

  def shutdown(): Unit = {
    graph.commit()
    try {
      graph.shutdown()
    } catch {
      case e: IllegalStateException => println("warning: " + e.toString)
    }
    println("databased shutdown")
  }

  def importUniprot(file: File): Unit = {
    if (!isModuleImported(UniprotKBModule)) {
      Import.importUniprot(file, graph)
      setAsImported(UniprotKBModule)
    } else {
      println("uniprot is already imported")
    }
  }

  def importHG19(file: File): Unit = {
    if (!isModuleImported(UniprotKBModule)) {
      println("error: UniprotKB should be imported first")
    } else if (!isModuleImported(HG19Module)) {
      val hg19 = TitanReference.getOrCreateReference(graph, "hg19")
      Import.ncbiPositionsImport(file, "GRCh37\\.p13", graph, hg19)
      setAsImported(HG19Module)
    } else {
      println("hg19 positions are already imported")
    }
  }

  def importHG38(file: File): Unit = {
    if (!isModuleImported(UniprotKBModule)) {
      println("error: UniprotKB should be imported first")
    } else if (!isModuleImported(HG38Module)) {
      val hg38 = TitanReference.getOrCreateReference(graph, "hg38")
      Import.ncbiPositionsImport(file, "GRCh38", graph, hg38)
      setAsImported(HG38Module)
    } else {
      println("hg38 positions are already imported")
    }
  }

  def isModuleImported(module: Module): Boolean = {
    val it = graph.query().has("label", "imported").has("module", module.name).vertices().iterator()
    it.hasNext
  }

  def setAsImported(module: Module): Unit = {
    val importVertex = graph.addVertexWithLabel("imported")
    importVertex.setProperty("module", module.name)
    importVertex.setProperty("fake", "fake")
    graph.commit()
  }

  def listImportedModules(): List[String] = {
    import scala.collection.JavaConversions._
    graph.query().has("label", "imported").has("fake", "fake").vertices().iterator().toList.map { v => v.getProperty("module").toString }
  }

  def importGeneSetDB(file: File) = {
    if (!isModuleImported(UniprotKBModule)) {
      println("error: UniprotKB should be imported first")
    } else if (isModuleImported(GeneSetDatabaseModule(GeneSetDB))) {
      println("warning: GeneSetDB has already been imported")
    } else {
      Import.importGenSetDB(file, graph)
      setAsImported(GeneSetDatabaseModule(GeneSetDB))
    }
  }

  def importIntPath(file: File) = {
    if (!isModuleImported(UniprotKBModule)) {
      println("error: UniprotKB should be imported first")
    } else if (isModuleImported(GeneSetDatabaseModule(IntPath))) {
      println("warning: IntPath has already been imported")
    } else {
      Import.importIntPath(file, graph)
      setAsImported(GeneSetDatabaseModule(IntPath))

    }
  }

  def importBioSystems(taxonomyFile: File, geneFile: File) = {
    if (!isModuleImported(UniprotKBModule)) {
      println("error: UniprotKB should be imported first")
    } else if (isModuleImported(GeneSetDatabaseModule(BioSystems))) {
      println("warning: BioSystems has already been imported")
    } else {
      BioSystemsImport.importBioSystemsTaxonomy(graph, taxonomyFile)
      graph.commit()
      BioSystemsImport.genesImport(graph, geneFile)

      setAsImported(GeneSetDatabaseModule(BioSystems))

    }
  }

  def importCustom(name: String, file: File) = {
    val module = GeneSetDatabaseModule(CustomDatabase(name))
    if (!isModuleImported(UniprotKBModule)) {
      println("error: UniprotKB should be imported first")
    } else if (isModuleImported(module)) {
      println("warning: " + module.name + " has already been imported")
    } else {
      BioSystemsImport.bioSystemsLikeGenesImport(CustomDatabase(name), graph, file)
      setAsImported(GeneSetDatabaseModule(CustomDatabase(name)))
    }
  }

  def downloadAndImportAll(workingDirectory: File): Unit = {
    val uniprotFile = new File(workingDirectory, "uniprot_sprot_human.dat.gz")
    Download.downloadUniprot(uniprotFile)
    importUniprot(uniprotFile)

    val bioSystemsTaxonomy = new File(workingDirectory, "biosystems_taxonomy.gz")
    val bioSystemsGenes = new File(workingDirectory, "biosystems_gene.gz")
    Download.downloadBioSystems(taxonomy = bioSystemsTaxonomy, gene = bioSystemsGenes)

    importBioSystems(taxonomyFile = bioSystemsTaxonomy, geneFile = bioSystemsGenes)

    val geneSetDB = new File(workingDirectory, "download-gmt_h.txt")
    Download.downloadGeneSetDB(geneSetDB)
    importGeneSetDB(geneSetDB)

    val intPath = new File("sapiens.zip")
    Download.downloadIntPath(intPath)
    Import.importIntPath(intPath, graph)
  }

}
