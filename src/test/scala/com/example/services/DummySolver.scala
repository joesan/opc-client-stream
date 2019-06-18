package com.example.services

/**
  * C1 needs 100 and uses -> P1, P2
  * C2 needs 80 and uses  -> P1, P2
  * C3 needs 90 and uses  -> P2
  *
  * P1 produces max 200 and P2 produces max 150
  *
  * So the linear equation would be:
  *
  * max(200 a + 150 b + 200 c + 150 d + 150 e)
  *
  * subject to constraints:
  *
  * 200 a + 150 b = 100
  * 200 c + 150 d = 80
  * 150 e = 90
  * a,b,c,d,e >= 0
  *
  * C1 uses a from P1 and b from P2 (a = 100, b = 0)
  * C2 uses c from P1 and d from P2 (c = 80, d = 0)
  * C3 uses e from P2 (e = 90)
  *
  *
  */
object DummySolver extends App {

  trait Monoid[A] {
    def op(a1: A, a2: A): A
    def identity: A
  }

  val equationMonoid: Monoid[Equation] = new Monoid[Equation] {
    def op(eq1: Equation, eq2: Equation): Equation =
      Equation(s"${eq1.lhs} + ${eq2.lhs}", eq1.constraints ++ eq2.constraints)

    def identity = Equation("", Seq.empty[String])
  }

  case class Equation(lhs: String, constraints: Seq[String])

  def maxFn(seq: Seq[Equation]): String = {
    seq.foldRight(equationMonoid.identity)(equationMonoid.op).lhs
  }

  def constraints(seq: Seq[Equation]): Seq[String] =
    seq.flatMap(elems => elems.constraints)
/*
  val testMaxFn = "200 a + 150 b + 200 c + 150 d + 150 e"
  val constraint1 = "200 a + 150 b = 100"
  val constraint2 = "200 c + 150 d = 80"
  val constraint3 = "150 e = 90"
  val constraint4 = "150 e = 90"
  val constraint5 = "150 e = 90"
  val constraint6 = "a >= 0"
  val constraint7 = "b >= 0"
  val constraint8 = "c >= 0"
  val constraint9 = "d >= 0"
  val constraint10 = "e >= 0"

  val constraints = Seq(
    constraint1,
    constraint2,
    constraint3,
    constraint4,
    constraint5,
    constraint6,
    constraint7,
    constraint8,
    constraint9,
    constraint10
  ) */

  import optimus.optimization._
  implicit val problem = LQProblem(SolverLib.ojalgo)
  val p1 = 200
  val p2 = 150
  val a = MPFloatVar("a", 0, 100)
  val b = MPFloatVar("b", 0, 100)
  val c = MPFloatVar("c", 0, 80)
  val d = MPFloatVar("d", 0, 80)
  val e = MPFloatVar("e", 0, 90)
  maximize(p1 * a + p2 * b + p1 * c + p2 * d + p2 * e )
  subjectTo(
    200 * a + 150 * b := 100,
    200 * c + 150 * d := 80,
    150 * e := 90,
    a >:= 0,
    b >:= 0,
    c >:= 0,
    d >:= 0,
    e >:= 0,
  )
  start()
  println("objective: " + objectiveValue)
  println("a = " + a.value.get * p1 + " b = " + b.value.get * p2 + " c = " + c.value.get * p1 + " d = " + d.value.get * p2 + " e = " + e.value.get * p2)
  release()
}
