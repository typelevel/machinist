package spire.ops

import scala.language.experimental.macros

trait Qux[A] {
  def plus(lhs: A, rhs: A): A
  def negate(lhs: A): A
  def eqv(lhs: A, rhs: A): Boolean
  def timesl(lhs: A, rhs: A): A
  def fromInt(n: Int): A
}

object Qux {
  implicit val int = new Qux[Int] {
    def plus(lhs: Int, rhs: Int): Int = lhs + rhs
    def negate(lhs: Int): Int = -lhs
    def eqv(lhs: Int, rhs: Int): Boolean = lhs == rhs
    def timesl(lhs: Int, rhs: Int): Int = lhs * rhs
    def fromInt(n: Int): Int = n
  }

  implicit class QuxOps1[A](x: A)(implicit ev: Qux[A]) {
    def +(rhs: A): A = macro SpireOps.binop[A, A]
    def unary_-(): A = macro SpireOps.unop[A]
    def ===(rhs: A): Boolean = macro SpireOps.binop[A, Boolean]
    def *:(lhs: A): A = macro SpireOps.rbinop[A, A]
    def +(rhs: Int): A = macro SpireOps.binopWithSelfLift[Int, Qux[A], A]
  }

  implicit class QuxOps2[A](x: A) {
    def +(rhs: A)(implicit ev: Qux[A]): A = macro SpireOps.binopWithEv[Qux[A], A, A]
    def unary_-(implicit ev: Qux[A]): A = macro SpireOps.unopWithEv[Qux[A], A]
    def ===(rhs: A)(implicit ev: Qux[A]): Boolean = macro SpireOps.binopWithEv[Qux[A], A, Boolean]
    def *:(lhs: A)(implicit ev: Qux[A]): A = macro SpireOps.rbinopWithEv[Qux[A], A, A]
  }
}

object Test1 {
  import Qux.QuxOps1

  def foo[A: Qux](a: A, b: A, c: A): A =
    if (a === b) -c
    else if (a === c) a + b
    else if (b === c) b *: c
    else b + 999

  println(foo(1, 2, 3))
}

object Test2 {
  import Qux.QuxOps2

  def foo[A: Qux](a: A, b: A, c: A): A =
    if (a === b) -c
    else if (a === c) a + b
    else if (b === c) b *: c
    else b + b

  println(foo(1, 2, 3))
}
