package exercise6

trait RNG {
  def nextInt: (Int, RNG)
}

case class SimpleRNG(seed: Long) extends RNG {
  def nextInt: (Int, RNG) = {
    val newSeed = (seed * 0x5deece66dL + 0xbL) & 0xffffffffffffL
    val nextRNG = SimpleRNG(newSeed)
    val n = (newSeed >>> 16).toInt
    (n, nextRNG)
  }
}

object RNG {
  def nonNegativeInt(rng: RNG): (Int, RNG) = rng.nextInt match {
    case (n, r) if n == Int.MinValue => (0, r)
    case (n, r) if n < 0 => (n * -1, r)
    case x => x
  }
  def double(rng: RNG): (Double, RNG) = map(nonNegativeInt)(i => i.toDouble / Int.MaxValue)(rng)
  def intDouble(rng: RNG): ((Int, Double), RNG) = {
    val (i, rng2) = rng.nextInt
    val (d, rng3) = double(rng2)
    ((i, d), rng3)
  }
  def doubleInt(rng: RNG): ((Double, Int), RNG) = {
    val (d, rng2) = double(rng)
    val (i, rng3) = rng2.nextInt
    ((d, i), rng3)
  }
  def double3(rng: RNG): ((Double, Double, Double), RNG) = {
    val (d1, rng2) = double(rng)
    val (d2, rng3) = double(rng2)
    val (d3, rng4) = double(rng3)
    ((d1, d2, d3), rng4)
  }
  def ints(count: Int)(rng: RNG): (List[Int], RNG) = sequence(List.fill(count)(int))(rng)

  type State[S, +A] = S => (A, S)
  type Rand[+A] = State[RNG, A]

  val int: Rand[Int] = _.nextInt

  def unit[A](a: A): Rand[A] = State.unit[RNG, A](a).run
  def map[A, B](ra: Rand[A])(f: A => B): Rand[B] = State(ra).map(f).run
  def nonNegativeEven: Rand[Int] = map(nonNegativeInt)(i => i - i % 2)
  def map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] = State(ra).map2(State(rb))(f).run
  def sequence[A](fs: List[Rand[A]]): Rand[List[A]] = State.sequence(fs.map(State(_))).run
  def nonNegativeLessThan(n: Int): Rand[Int] = flatMap(nonNegativeInt) { i =>
    val mod = i % n
    if (i + (n - 1) - mod >= 0) rng2 => (mod, rng2)
    else nonNegativeLessThan(n)
  }
  def flatMap[A, B](ra: Rand[A])(f: A => Rand[B]): Rand[B] = State(ra).flatMap(a => State(f(a))).run
}

case class State[S, +A](run: S => (A, S)) {
  def map[B](f: A => B): State[S, B] = flatMap(a => State(s => (f(a), s)))
  def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] = flatMap(a => sb.flatMap(b => State(s => ((f(a, b), s)))))
  def flatMap[B](f: A => State[S, B]): State[S, B] = State(s => {
    val (a, s2) = run(s)
    f(a).run(s2)
  })
}
object State {
  def unit[S, A](a: A): State[S, A] = State(s => (a, s))
  def sequence[S, A](fs: List[State[S, A]]): State[S, List[A]] = State(s => {
    @annotation.tailrec
    def f(s2: S, rs: List[State[S, A]], as: List[A]): (List[A], S) = rs match {
      case Nil => (as.reverse, s2)
      case h :: t => {
        val (a, s3) = h.run(s2)
        f(s3, t, a :: as)
      }
    }
    f(s, fs, Nil)
  })
  def get[S]: State[S, S] = State(s => (s, s))
  def set[S](s: S): State[S, Unit] = State(_ => ((), s))
  def modify[S](f: S => S): State[S, Unit] = for {
    s <- get
    _ <- set(f(s))
  } yield ()
}

sealed trait Input
case object Coin extends Input
case object Turn extends Input
case class Machine(locked: Boolean, candies: Int, coins: Int) {
  def accept(input: Input): Machine = (this, input) match {
    case (Machine(true, candies, coins), Coin) if candies > 0 => Machine(false, candies, coins + 1)
    case (Machine(false, candies, coins), Turn) if candies > 0 => Machine(true, candies - 1, coins)
    case (machine, _) => machine
  }
}
object Machine {
  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] = {
    State(machine => {
      val m = inputs.foldLeft(machine)(_.accept(_))
      ((m.coins, m.candies), m)
    })
  }
}
