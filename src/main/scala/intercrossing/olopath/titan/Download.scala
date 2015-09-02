package intercrossing.olopath.titan

import java.io.File
import java.net.URL

object Download {

  def download(name: String, url: URL, file: File, downloadTwice: Boolean): Unit = {
    import sys.process._
    println("downloading " + name + " from " + url)
    if (!file.exists()) {
      (url #> file).!!
      println("downloaded")
    } else {
      println(file.getName + " already downloaded")
    }
  }

  def downloadUniprot(file: File): Unit = {
    val uniprotURL = new URL("ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/uniprot_sprot_human.dat.gz")

    download("uniprot", uniprotURL, file, false)
  }

  def downloadHG19(file: File): Unit = {
    val url = new URL("ftp://ftp.ncbi.nih.gov/genomes/H_sapiens/ARCHIVE/ANNOTATION_RELEASE.105/mapview/seq_gene.md.gz")
    download("hg19", url, file, false)
  }

  def downloadHG38(file: File): Unit = {
    val url = new URL("ftp://ftp.ncbi.nih.gov/genomes/H_sapiens/mapview/seq_gene.md.gz")
    download("hg38", url, file, false)
  }

  def downloadGeneSetDB(file: File): Unit = {
    val url = new URL("http://www.genesetdb.auckland.ac.nz/download.php?filename=download/gmt_h")
    download("GeneSetDB", url, file, false)
  }

  def downloadBioSystems(taxonomy: File, gene: File): Unit = {
    val geneURL = new URL("ftp://ftp.ncbi.nih.gov/pub/biosystems/CURRENT/biosystems_gene.gz")
    val taxonomyURL = new URL("ftp://ftp.ncbi.nih.gov/pub/biosystems/CURRENT/biosystems_taxonomy.gz")

    download("BioSystems taxonomy", taxonomyURL, taxonomy, false)
    download("BioSystems genes", geneURL, gene, false)

  }


  def downloadIntPath(file: File): Unit = {
    val url = new URL("http://compbio.ddns.comp.nus.edu.sg:8080/IntPath/dataset/database/sapiens.zip")
    download("IntPath sapiens.zip", url, file, false)
  }

  def downloadReactomeAll(file: File): Unit = {
    val url = new URL("http://www.reactome.org/download/current/UniProt2Reactome_All_Levels.txt")
    download("ReactomeAll", url, file, false)
  }

  def downloadReactome(file: File): Unit = {
    val url = new URL("http://www.reactome.org/download/current/UniProt2Reactome.txt")
    download("Reactome", url, file, false)
  }

  def downloadPID(file: File): Unit = {
    val url = new URL("ftp://ftp1.nci.nih.gov/pub/PID/uniprot/uniprot.tab.gz")
    download("PID", url, file, false)
  }
}
