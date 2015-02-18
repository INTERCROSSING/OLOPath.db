package intercrossing.olopath

import java.io.{PrintWriter, File}

import scala.collection.mutable

/**
 * Created by Evdokim on 01.02.2015.
 */
object GeneSetClustering {

  def findClosest(d0: Double, geneSet: mutable.ArrayBuffer[Long], sets2: mutable.HashMap[String, mutable.ArrayBuffer[Long]])
  : Option[(String, mutable.ArrayBuffer[Long])] = {
    sets2.find { case (name, set2) =>
      SetUtils.distance(geneSet, set2) <= d0
    }
  }

  def saveGeneSets(geneSets: mutable.HashMap[String, mutable.ArrayBuffer[Long]], file: File) {
    println("saving " + geneSets.size + " to " + file.getPath)
    val out = new PrintWriter(file)
    geneSets.foreach { case (name, set) =>
      set.foreach { geneID =>
        out.println(name + "\t" + geneID + "\t" + 100)
      }
    }
    out.close()
    println("saved")
  }


  def compare(geneSets1: mutable.HashMap[String, mutable.ArrayBuffer[Long]],
              geneSets2: mutable.HashMap[String, mutable.ArrayBuffer[Long]], d0: Double,
              uniqueFile: File, commonFile: File): Unit = {

    println("comparing " + geneSets1.size + " gene sets vs " + geneSets2.size + " gene sets")
    var unique = 0L
    var notUnique = 0L
    val uniqueOut = new PrintWriter(uniqueFile)
    val commonOut = new PrintWriter(commonFile)
    geneSets1.foreach { case (name1, set1) =>

      if ((unique + notUnique) % 100 == 0) {
        println("comparing unique: " + unique + " common: " + notUnique)
      }
      findClosest(d0, set1, geneSets2) match {
        case None => {
          set1.foreach { geneID =>
            uniqueOut.println(name1 + "\t" + geneID + "\t" + 100)
          }
          unique += 1
        }
        case Some((name, set)) => {
          notUnique += 1
          set1.foreach { geneID =>
            commonOut.println(name1 + "\t" + geneID + "\t" + 100)
          }
        }
      }
    }

    commonOut.close()
    uniqueOut.close()
    println("compared unique: " + unique + " common: " + notUnique)
  }

  def cluster(geneSets: mutable.HashMap[String, mutable.ArrayBuffer[Long]], d0: Double, maxSize: Option[Int] = None): mutable.HashMap[String, mutable.ArrayBuffer[Long]] = {
    val result = new mutable.HashMap[String, mutable.ArrayBuffer[Long]]
    println("clustering " + geneSets.size + " gene sets")

    var processed = 0
    geneSets.foreach { case (name, set) =>
      processed += 1
      if (processed % 100 == 0) {
        println("clustering " + processed + " processed")
      }
      findClosest(d0, set, result) match {
        case None => {
          //add to result
          result.put(name, set)
        }
        case Some((name2, set2)) => {
          result.remove(name2)
          val union = SetUtils.union(set2, set)
          val chunk = maxSize match {
            case None => union
            case Some(m) => union.take(m)
          }
          result.put(name2 + "_" + name, chunk)
        }
      }
    }

    println("clustered to " + result.size + " gene sets")
    result
  }


}
