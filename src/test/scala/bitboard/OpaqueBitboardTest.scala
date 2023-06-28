package chess
package bitboard

import munit.ScalaCheckSuite
import org.scalacheck.Prop.{ forAll, propBoolean }

import Arbitraries.given
import Bitboard.*

class OpaqueBitboardTest extends ScalaCheckSuite:

  test("the result of addSquare should contain the added square"):
    forAll: (bb: Bitboard, square: Square) =>
      assertEquals((bb.addSquare(square).contains(square)), true)

  test("Square.bb.singleSquare == Some(square)"):
    forAll: (square: Square) =>
      assertEquals(square.bb.singleSquare, Some(square))

  test("count has the same value as squares.size"):
    forAll: (bb: Bitboard) =>
      assertEquals(bb.count, bb.squares.size)

  test("moreThanOne should be true when count > 1"):
    forAll: (bb: Bitboard) =>
      assertEquals(bb.moreThanOne, bb.count > 1)

  test("if moreThanOne then first is defined"):
    forAll: (bb: Bitboard) =>
      bb.moreThanOne ==> bb.first.isDefined

  test("singleSquare should be defined when count == 1"):
    forAll: (bb: Bitboard) =>
      assertEquals(bb.singleSquare.isDefined, bb.count == 1)

  test("fold removeFirst should return empty"):
    forAll: (bb: Bitboard) =>
      assertEquals(bb.fold(bb)((b, _) => b.removeFirst), Bitboard.empty)

  test("first with a function that always return None should return None"):
    forAll: (bb: Bitboard) =>
      assertEquals(bb.first(_ => None), None)

  test("first with a function that always return Some should return the first square"):
    forAll: (bb: Bitboard) =>
      assertEquals(bb.first(Some(_)), bb.first)

  test("first returns the correct square"):
    forAll: (xs: Set[Square], x: Square) =>
      def f = (p: Square) => if p == x then Some(p) else None
      Bitboard(xs + x).first(f) == Some(x)

  test("bitboard created by set of square should contains and only contains those square"):
    forAll: (xs: Set[Square]) =>
      val bb = Bitboard(xs)
      assertEquals(xs.forall(bb.contains), true)
      assertEquals(xs.size, bb.count)

  test("apply set of square should be the same as using addSquare"):
    forAll: (xs: Set[Square]) =>
      val bb  = Bitboard(xs)
      val bb2 = xs.foldLeft(Bitboard.empty)(_ addSquare _)
      assertEquals(bb, bb2)

  test("addSquare andThen removeSquare should be the same as identity"):
    forAll: (bb: Bitboard, square: Square) =>
      !bb.contains(square) ==> assertEquals(bb.addSquare(square).removeSquare(square), bb)

  test("move == addSquare. removeSquare"):
    forAll: (bb: Bitboard, from: Square, to: Square) =>
      from != to ==> {
        bb.move(from, to) == bb.addSquare(to).removeSquare(from)
      }

  test("apply(Set[square]).squares.toSet == Set[square]"):
    forAll: (xs: Set[Square]) =>
      val result = Bitboard(xs).squares.toSet
      assertEquals(result, xs)

  test("nonEmpty bitboard should have at least one square"):
    forAll: (bb: Bitboard) =>
      assertEquals(bb.nonEmpty, bb.first.isDefined)

  test("first should be the minimum of squares"):
    forAll: (bb: Bitboard) =>
      assertEquals(bb.first, bb.squares.minByOption(_.value))

  test("intersects should be true when the two bitboards have at least one common square"):
    forAll: (b1: Bitboard, b2: Bitboard, p: Square) =>
      b1.addSquare(p).intersects(b2.addSquare(p))

  test("intersects and set intersection should be consistent"):
    forAll: (s1: Set[Square], s2: Set[Square]) =>
      val b1 = Bitboard(s1)
      val b2 = Bitboard(s2)
      val s  = s1 intersect s2
      b1.intersects(b2) == s.nonEmpty

  test("isDisjoint should be false when the two bitboards have at least common square"):
    forAll: (b1: Bitboard, b2: Bitboard, p: Square) =>
      !b1.addSquare(p).isDisjoint(b2.addSquare(p))

  test("isDisjoint and intersects always return the opposite value"):
    forAll: (b1: Bitboard, b2: Bitboard) =>
      b1.isDisjoint(b2) == !b1.intersects(b2)

  property("forall"):
    forAll: (b: Bitboard, f: Square => Boolean) =>
      b.forall(f) == b.squares.forall(f)

  property("exists"):
    forAll: (b: Bitboard, f: Square => Boolean) =>
      b.exists(f) == b.squares.exists(f)

  property("map"):
    forAll: (b: Bitboard, f: Square => Int) =>
      b.map(f) == b.squares.map(f)

  property("flatMap"):
    forAll: (b: Bitboard, f: Square => Option[Int]) =>
      b.flatMap(f) == b.squares.flatMap(f)

  property("filter"):
    forAll: (b: Bitboard, f: Square => Boolean) =>
      b.filter(f) == b.squares.filter(f)

  property("first"):
    forAll: (b: Bitboard, f: Square => Option[Int]) =>
      b.first(f) == b.squares.map(f).find(_.isDefined).flatten

  property("foreach"):
    forAll: (b: Bitboard, f: Square => Int) =>
      var s1 = 0L
      var s2 = 0L
      b.foreach(x => s1 += f(x).toLong)
      b.squares.foreach(x => s2 += f(x).toLong)
      s1 == s2
