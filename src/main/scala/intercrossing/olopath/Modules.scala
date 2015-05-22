package intercrossing.olopath

import intercrossing.olopath.titan.GeneSetDatabase

abstract class Module(val name: String)

case class GeneSetDatabaseModule(database: GeneSetDatabase) extends Module(database.name)

case object UniprotKBModule extends Module("uniprot")

case object HG19Module extends Module("hg19")

case object HG38Module extends Module("hg38")