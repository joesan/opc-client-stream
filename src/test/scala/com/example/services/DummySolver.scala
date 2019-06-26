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
  * Problem:
  * How much power does C1 needs from P1 and P2 to satisfy his 100
  * Let a be the amount of power C1 takes from P1 and b be the amount of power c1 takes from p2
  * a + b = 100
  *
  * Let c be the amount of power C2 takes from P1 and d be the ampunt of Power consumer C2 takes from p2
  * c + d = 80
  *
  * Let e be the amount of power C3 takes from P2
  * e = 90
  *
  * constraints:
  * a + c <= 200
  * b + d + e <= 150
  *
  * max(a + b + c + d + e)
  *
  *
  *
Model oJalgo: 8x5
Configuring variable bounds...
Adding objective function...
Creating constraints: Added 8 constraints in 1ms
Solving...
Solution status is Optimal
objective: 270.0
a = 0.0    b = 100.00000000005001 c = 0.0   d = 79.99999999995 e = 90.0 (Jothi)
a = 100.0  b = 0.0                c = 20.0  d = 60.0           e = 90.0 (Clemens)
a = 64     b = 35                 c = 51.0  d = 28.0           e = 90.0 (Excel)
a = 55.0   b = 45.0               c = 80.0  d = 0.0            e = 90.0 (Clemens 2)
  *
  * (a + c) / 270 = s1
  * (b + d + e) / 270 = s2
  *
  * sMax > s1
  * sMax > s2
  *
  * P1 -> (C1 @ 40%, C2 @ 40%, C3 @ 20%)
  * P2 -> (C1 @
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
  val s1 = MPFloatVar("s1", 0, 1)
  val s2 = MPFloatVar("s2", 0, 1)
  val a = MPFloatVar("a", 0, 100)
  val b = MPFloatVar("b", 0, 100)
  val c = MPFloatVar("c", 0, 80)
  val d = MPFloatVar("d", 0, 80)
  val e = MPFloatVar("e", 0, 90)
  val sMax = MPFloatVar("sMax", 0, 350)
  maximize(a +  b +  c + d + e - sMax)
  subjectTo(
    (a + c) := s1 * 270,
    (b + d + e) := s2 * 270,
    a + c <:= 200,
    b + d + e <:= 150,
    a + b := 100,
    c + d := 80,
    e := 90,
    sMax >:= s2,
    sMax >:= s1,
    //200 * a + 150 * b := 100, // Consumer 1
    //200 * c + 150 * d := 80, // Consumer 2
    //150 * e := 90,
    a >:= 0,
    b >:= 0,
    c >:= 0,
    d >:= 0,
    e >:= 0,
  )

  start()
  println("objective: " + objectiveValue)
  //println("a = " + a.value.get * p1 + " b = " + b.value.get * p2 + " c = " + c.value.get * p1 + " d = " + d.value.get * p2 + " e = " + e.value.get * p2)
  println("a = " + a.value.get + " b = " + b.value.get  + " c = " + c.value.get + " d = " + d.value.get + " e = " + e.value.get)
  println(s1.value.get)
  println(s2.value.get)
  println(s1.value.get + s2.value.get)
  println(sMax.value.get)
  release()
}
