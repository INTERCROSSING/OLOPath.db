package intercrossing.olopath

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object SetUtils {

  def xor(seq1: Seq[Long], seq2: Seq[Long]): Int = {
    @tailrec
    def xorrec(i1: Int, i2: Int, acc: Int): Int = {
      if (i1 >= seq1.length) {
        acc + math.max(0, seq2.length - i2)
      } else if (i2 >= seq2.length) {
        acc + math.max(0, seq1.length - i1)
      } else {
        if(seq1(i1) == seq2(i2)) {
          xorrec(i1 + 1, i2 + 1, acc)
        } else if (seq1(i1) > seq2(i2)) {
          xorrec(i1, i2 + 1, acc + 1)
        } else {
          xorrec(i1 + 1, i2, acc + 1)
        }
      }
    }
    xorrec(0, 0, 0)
  }

  def distance(seq1: Seq[Long], seq2: Seq[Long]): Double = {
    (xor(seq1, seq2) + 0D) / (seq1.length + seq2.length + 0D)
  }

  def isSorted(list: mutable.ArrayBuffer[Long]): (Boolean, (Long, Long)) = {
    var res = true
    var wrongPair = (0L, 1L)
    for (i <- 0 to (list.size - 2)) {
      if(list(i) > list(i+1)) {
        wrongPair = (list(i), list(i+1))
        res =  false
      }
    }
    (res, wrongPair)
  }

  def union(seq1: Seq[Long], seq2: Seq[Long]): mutable.ArrayBuffer[Long] = {

    @tailrec
    def unionrec(i1: Int, i2: Int, res: mutable.ArrayBuffer[Long]): Unit = {
      if (i1 >= seq1.length) {
        res ++= seq2.drop(i2)
      } else if (i2 >= seq2.length) {
        res ++= seq1.drop(i1)
      } else {
        if(seq1(i1) == seq2(i2)) {
          res += seq1(i1)
          unionrec(i1 + 1, i2 + 1, res)
        } else if (seq1(i1) > seq2(i2)) {
          res += seq2(i2)
          unionrec(i1, i2 + 1, res)
        } else {
          res += seq1(i1)
          unionrec(i1 + 1, i2, res)
        }
      }
    }

    val res = new mutable.ArrayBuffer[Long](seq1.length + seq2.length)
    unionrec(0, 0, res)
    res

  }
}
