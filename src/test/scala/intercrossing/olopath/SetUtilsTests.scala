package intercrossing.olopath

import intercrossing.olopath.titan._
import org.junit.Test
import org.junit.Assert._
import scala.collection.JavaConversions._



class SetUtilsTests {

  @Test
  def xor1Test(): Unit = {
    val seq1 = List(1L)
    val seq2 = List(0L)
    assertEquals(2, SetUtils.xor(seq1, seq2))
  }

  @Test
  def xor2Test(): Unit = {
    val seq1 = List(1L, 2L, 3L)
    val seq2 = List(0L, 2L)
    assertEquals(3, SetUtils.xor(seq1, seq2))
  }

  @Test
  def xor3Test(): Unit = {
    val seq1 = List(1L, 2L, 3L)
    val seq2 = List(100L, 200L)
    assertEquals(5, SetUtils.xor(seq1, seq2))
  }


  @Test
  def xor4Test(): Unit = {
    val seq1 = List(1L, 2L, 3L)
    val seq2 = List(1L, 2L, 3L)
    assertEquals(0, SetUtils.xor(seq1, seq2))
  }

  @Test
  def unionTest1(): Unit = {
    val seq1 = List[Long]()
    val seq2 = List[Long]()
    assertEquals(List[Long](), SetUtils.union(seq1, seq2).toList)
  }

  @Test
  def unionTest2(): Unit = {
    val seq1 = List[Long](1L)
    val seq2 = List[Long]()
    assertEquals(List[Long](1L), SetUtils.union(seq1, seq2).toList)
  }


  @Test
  def unionTest3(): Unit = {
    val seq1 = List[Long](1, 2)
    val seq2 = List[Long](2, 3)
    assertEquals(List[Long](1, 2, 3), SetUtils.union(seq1, seq2).toList)
  }

  @Test
  def unionTest4(): Unit = {
    val seq1 = List[Long](1, 2, 3)
    val seq2 = List[Long](1, 2, 3)
    assertEquals(List[Long](1, 2, 3), SetUtils.union(seq1, seq2).toList)
  }

  @Test
  def unionTest5(): Unit = {
    val seq1 = List[Long](1, 3, 5)
    val seq2 = List[Long](2, 4, 6)
    assertEquals(List[Long](1, 2, 3, 4, 5, 6), SetUtils.union(seq1, seq2).toList)
  }

}
