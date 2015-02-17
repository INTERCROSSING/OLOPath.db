package intercrossing.olopath

trait Chromosome {
  def name: String

  def getGenes: List[Gene]
}
