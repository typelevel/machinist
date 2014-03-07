package spire.ops

import scala.language.higherKinds

import scala.reflect.macros.Context

/**
 * This trait has some nice methods for working with implicit Ops
 * classes. It is used to rewrite implicit conversions which "enrich"
 * a type with operators into code that does not allocate an implicit
 * instance.
 */
trait Ops {

  /**
   * Given context, this method rewrites the tree to call the desired
   * method with the lhs parameter. We find the symbol which is
   * applying the macro and use its name to determine what method to
   * call.
   *
   * If we see code like:
   *
   *   -lhs
   *
   * After typing and implicit resolution, we get trees like:
   *   
   *   conversion(lhs)(ev).unary_-(): R
   *
   * The macro should produce trees like:
   *
   *   ev.negate(lhs): R
   */
  def unop[R](c: Context)(): c.Expr[R] = {
    import c.universe._
    val (ev, lhs) = unpack(c)
    c.Expr[R](Apply(Select(ev, findMethodName(c)), List(lhs)))
  }

  /**
   * Like unop, but with ev provided to the method instead of to the
   * implicit constructor.
   * 
   * If we see code like:
   * 
   *   lhs.abs
   * 
   * After typing and implicit resolution, we get trees like:
   *   
   *   conversion(lhs).abs(ev: Ev): R
   *
   * The macro should produce trees like:
   *
   *   ev.abs(lhs): R
   */
  def unopWithEv[Ev, R](c: Context)(ev: c.Expr[Ev]): c.Expr[R] = {
    import c.universe._
    val lhs = unpackWithoutEv(c)
    c.Expr[R](Apply(Select(ev.tree, findMethodName(c)), List(lhs)))
  }

  /**
   * Given context and an expression, this method rewrites the tree to
   * call the "desired" method with the lhs and rhs parameters. We
   * find the symbol which is applying the macro and use its name to
   * determine what method to call.
   *
   * If we see code like:
   *
   *   lhs + rhs
   *
   * After typing and implicit resolution, we get trees like:
   *   
   *   conversion(lhs)(ev).$plus(rhs: A): R
   *
   * The macro should produce trees like:
   *
   *   ev.method(lhs: A, rhs: A): R
   */
  def binop[A, R](c: Context)(rhs: c.Expr[A]): c.Expr[R] = {
    import c.universe._
    val (ev, lhs) = unpack(c)
    c.Expr[R](Apply(Select(ev, findMethodName(c)), List(lhs, rhs.tree)))
  }

  /**
   * Like binop, but for right-associative operators (eg. +:).
   * 
   * If we see code like:
   * 
   *   lhs *: rhs
   * 
   * After typing and implicit resolution, we get trees like:
   * 
   *   conversion(rhs)(ev).$times$colon(lhs)
   * 
   * The macro should produce trees like:
   * 
   *   ev.timesl(lhs, rhs)
   */
  def rbinop[A, R](c: Context)(lhs: c.Expr[A]): c.Expr[R] = {
    import c.universe._
    val (ev, rhs) = unpack(c)
    c.Expr[R](Apply(Select(ev, findMethodName(c)), List(lhs.tree, rhs)))
  }

  /**
   * Like binop, but with ev provided to the method instead of to the
   * implicit constructor.
   * 
   * If we see code like:
   * 
   *   lhs % rhs
   * 
   * After typing and implicit resolution, we get trees like:
   * 
   *   conversion(lhs).$percent(rhs)(ev)
   * 
   * The macro should produce trees like:
   * 
   *   ev.mod(lhs, rhs)
   */
  def binopWithEv[A, Ev, R](c: Context)(rhs: c.Expr[A])(ev: c.Expr[Ev]): c.Expr[R] = {
    import c.universe._
    val lhs = unpackWithoutEv(c)
    c.Expr[R](Apply(Select(ev.tree, findMethodName(c)), List(lhs, rhs.tree)))
  }

  /**
   * Like rbinop, but with ev provided to the method instead of to the
   * implicit constructor.
   * 
   * If we see code like:
   * 
   *   lhs *: rhs
   * 
   * After typing and implicit resolution, we get trees like:
   * 
   *   conversion(rhs).$times$colon(lhs)(ev)
   * 
   * The macro should produce trees like:
   * 
   *   ev.timesl(lhs, rhs)
   */
  def rbinopWithEv[A, Ev, R](c: Context)(lhs: c.Expr[A])(ev: c.Expr[Ev]): c.Expr[R] = {
    import c.universe._
    val rhs = unpackWithoutEv(c)
    c.Expr[R](Apply(Select(ev.tree, findMethodName(c)), List(lhs.tree, rhs)))
  }

  /**
   * Combine an implicit enrichment with a lifting method.
   * 
   * If we see code like:
   * 
   *   lhs + 1
   * 
   * After typing and implicit resolution, we get trees like:
   * 
   *   conversion(lhs)(ev0).$plus(1)(ev1): R
   * 
   * The macro should produce trees like:
   * 
   *   ev0.plus(lhs, ev1.fromInt(1))
   * 
   * In Spire, this let's us use Ring's fromInt method and
   * ConvertableTo's fromDouble (etc.) before applying an
   * op. Eventually, we should generalize the way we choose the
   * lifting method.
   */
  def binopWithLift[A: c.WeakTypeTag, Ev, R](c: Context)(rhs: c.Expr[A])(ev1: c.Expr[Ev]): c.Expr[R] = {
    import c.universe._
    val (ev0, lhs) = unpack(c)
    val typeName = weakTypeOf[A].typeSymbol.name
    val rhs1 = Apply(Select(ev1.tree, newTermName("from" + typeName)), List(rhs.tree))
    c.Expr[R](Apply(Select(ev0, findMethodName(c)), List(lhs, rhs1)))
  }

  /**
   * This is like binopWithLift, but we use the same evidence
   * parameter to make the method call and do the lifting.
   * 
   * If we see code like:
   * 
   *   lhs * 2
   * 
   * After typing and implicit resolution, we get trees like:
   * 
   *   conversion(lhs)(ev).$times(2): R
   * 
   * The macro should produce trees like:
   * 
   *   ev.times(lhs, ev.fromInt(2))
   */
  def binopWithSelfLift[A: c.WeakTypeTag, Ev, R](c: Context)(rhs: c.Expr[A]): c.Expr[R] = {
    import c.universe._
    val (ev0, lhs) = unpack(c)
    val typeName = weakTypeOf[A].typeSymbol.name
    val rhs1 = Apply(Select(ev0, newTermName("from" + typeName)), List(rhs.tree))
    c.Expr[R](Apply(Select(ev0, findMethodName(c)), List(lhs, rhs1)))
  }

  /**
   * Similar to binop, but for situations where there is no evidence
   * parameter, and we just want to call a method on the rhs.
   * 
   * After typing and implicit resolution, we get trees like:
   * 
   *   conversion(lhs).foo(rhs)
   * 
   * and we want to get out:
   * 
   *   rhs.foo(lhs)
   */
  def flip[A, R](c: Context)(rhs: c.Expr[A]): c.Expr[R] = {
    import c.universe._
    val lhs = unpackWithoutEv(c)
    c.Expr[R](Apply(Select(rhs.tree, findMethodName(c)), List(lhs)))
  }

  /**
   * Given context, this method pulls the 'ev' and 'lhs' values out of
   * instantiations of implicit -Ops classes.
   * 
   * For instance, given a tree like:
   * 
   *   new FooOps(x)(ev)
   * 
   * This method would return (ev, x).
   */
  def unpack[T[_], A](c: Context) = {
    import c.universe._
    c.prefix.tree match {
      case Apply(Apply(TypeApply(_, _), List(x)), List(ev)) => (ev, x)
      case t => c.abort(c.enclosingPosition,
        "Cannot extract subject of operator (tree = %s)" format t)
    }
  }

  /**
   * Given context, this method pulls the 'lhs' value out of
   * instantiations of implicit -Ops classes.
   * 
   * For instance, given a tree like:
   * 
   *   new FooOps(x)
   * 
   * This method would return x.
   */
  def unpackWithoutEv(c: Context) = {
    import c.universe._
    c.prefix.tree match {
      case Apply(TypeApply(_, _), List(lhs)) => lhs
      case t => c.abort(c.enclosingPosition,
        "Cannot extract subject of operator (tree = %s)" format t)
    }
  }

  /**
   * Provide a canonical mapping between "operator names" used in Ops
   * classes and the actual method names used for type classes.
   *
   * This is an interesting directory of the operators Spire
   * supports. It's also worth noting that we don't (currently) have
   * the capacity to dispatch to two different typeclass-method names
   * for the same operator--typeclasses have to agree to use the same
   * name for the same operator.
   *
   * In general "textual" method names should just pass through to the
   * typeclass... it is probably not wise to provide mappings for them
   * here.
   */
  def findMethodName(c: Context) = {
    import c.universe._
    val s = c.macroApplication.symbol.name.toString
    newTermName(operatorNames.getOrElse(s, s))
  }

  /**
   * Map of symbolic -> textual name conversions.
   * 
   * If this map is empty, the macros will not do any special
   * rewriting and all names will be passed through.
   * 
   * Symbolic names should be written as Scala would represent them
   * internally. For example, + should be written as $plus.
   */
  def operatorNames: Map[String, String]
}

trait SpireOperatorNames {

  val operatorNames = Map(
    // Eq (=== =!=)
    ("$eq$eq$eq", "eqv"),
    ("$eq$bang$eq", "neqv"),

    // Order (> >= < <=)
    ("$greater", "gt"),
    ("$greater$eq", "gteqv"),
    ("$less", "lt"),
    ("$less$eq", "lteqv"),

    // Semigroup (|+| |-|)
    ("$bar$plus$bar", "op"),
    ("$bar$minus$bar", "opInverse"),

    // Ring (unary_- + - * **)
    ("unary_$minus", "negate"),
    ("$plus", "plus"),
    ("$minus", "minus"),
    ("$times", "times"),
    ("$times$times", "pow"),

    // EuclideanRing (/~ % /%)
    ("$div$tilde", "quot"),
    ("$percent", "mod"),
    ("$div$percent", "quotmod"),

    // Field (/)
    ("$div", "div"),

    // BooleanAlgebra (^ | & ~)
    ("$up", "xor"),
    ("$bar", "or"),
    ("$amp", "and"),
    ("unary_$tilde", "complement"),

    // BitString (<< >> >>>)
    ("$less$less", "leftShift"),
    ("$greater$greater$greater", "rightShift"),
    ("$greater$greater", "signedRightShift"),

    // VectorSpace (*: :* :/ ⋅)
    ("$times$colon", "timesl"),
    ("$colon$times", "timesr"),
    ("$colon$div", "divr"),
    ("$u22C5", "dot")
  )
}
