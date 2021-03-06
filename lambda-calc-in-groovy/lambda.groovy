
//Zコンビネータ
//Z = λf. (λx. f (λy. x x y)) (λx. f (λy. x x y))
Z = { f -> ({ x -> f({ y -> x(x)(y) }) })({ x -> f({ y -> x(x)(y) }) }) }

//とりあえず数値を定義してみる
ZERO  = { f -> { x -> x }}
ONE   = { f -> { x -> f(x) }}
TWO   = { f -> { x -> f(f(x)) }}
THREE = { f -> { x -> f(f(f(x))) }}
FOUR  = { f -> { x -> f(f(f(f(x)))) }}
FIVE  = { f -> { x -> f(f(f(f(f(x))))) }}

//数値関数いろいろ
SUCC = { n -> { f -> { x -> f(n(f)(x)) }}}
ADD  = { m -> { n -> { f -> { x -> m(f)(n(f)(x)) }}}}
PRED = { n -> LEFT(n({ x -> PAIR(RIGHT(x))(SUCC(RIGHT(x))) })(PAIR(ZERO)(ZERO))) }
SUB  = { m -> { n -> n(PRED)(m) }}
MUL  = { m -> { n -> n(ADD(m))(ZERO) }}
DIV  = { m -> { n ->
    Z({ f -> { x -> { y ->
        IF(IS_LESS_THAN(x)(n))(y)({ z -> f(SUB(x)(n))(SUCC(y))(z) }) 
    }}})(m)(ZERO)
}}
MOD  = { m -> { n ->
    Z({ f -> { x ->
        IF(IS_LESS_THAN(x)(n))(x)({ z -> f(SUB(x)(n))(z) }) 
    }})(m)
}}

IS_ZERO = { n -> n({ x -> FALSE })(TRUE) }
IS_LESS_EQUAL = { m -> { n -> IS_ZERO(SUB(m)(n)) }}
IS_EQUAL = { m -> { n -> AND(IS_LESS_EQUAL(m)(n))(IS_LESS_EQUAL(n)(m)) }}
IS_LESS_THAN = { m -> { n -> AND(IS_LESS_EQUAL(m)(n))(NOT(IS_EQUAL(m)(n))) }}

//真偽値
TRUE  = { t -> { f -> t }}
FALSE = { t -> { f -> f }}
IF    = { b -> { x -> { y -> b(x)(y) }}}
AND   = { a -> { b -> a(b)(FALSE) }}
NOT   = { b -> { t -> { f -> b(f)(t) }}}

//ペア
PAIR  = { l -> { r -> { f -> f(l)(r) }}}
LEFT  = { p -> p(TRUE) }
RIGHT = { p -> p(FALSE) }

//リスト
CONS   = { h -> { t -> PAIR(PAIR(FALSE)(h))(t) }}
NIL    = PAIR(PAIR(TRUE)(TRUE))(TRUE)
IS_NIL = { l -> LEFT(LEFT(l)) }
HEAD   = { l -> RIGHT(LEFT(l)) }
TAIL   = { l -> RIGHT(l) }
APPEND = Z({ f -> { l -> { m ->
    IF(IS_NIL(l))(
        IF(IS_NIL(m))(
            NIL
        )(
            { x -> CONS(HEAD(m))(f(l)(TAIL(m)))(x) }
        )
    )(
        { x -> CONS(HEAD(l))(f(TAIL(l))(m))(x) }
    )
}}})
FLAT_MAP = { f -> FOLDL(NIL)({ l -> { x -> APPEND(l)(f(x)) }}) }
FOLDL = Z({ f -> { z -> { g -> { l ->
    IF(IS_NIL(l))(
        z
    )(
        { x -> f(g(z)(HEAD(l)))(g)(TAIL(l))(x) }
    )
}}}})


//intに変換する関数
def toInt(n) { n({ it + 1 })(0) }

//booleanに変換する関数
def toBoolean(b) { b(true)(false) }

//Listに変換する関数
def toList(l, f) {
    def xs = []
    while(toBoolean(IS_NIL(l)) == false) {
        def h = HEAD(l)
        xs += f(h)
        l = TAIL(l)
    }
    xs
}



//数値
assert(toInt(ZERO)  == 0)
assert(toInt(ONE)   == 1)
assert(toInt(TWO)   == 2)
assert(toInt(THREE) == 3)

assert(toInt(SUCC(THREE))     == 4)
assert(toInt(ADD(TWO)(THREE)) == 5)
assert(toInt(PRED(THREE))     == 2)
assert(toInt(SUB(THREE)(TWO)) == 1)
assert(toInt(SUB(ONE)(TWO))   == 0) //どんだけ引いても最小値は0
assert(toInt(MUL(TWO)(THREE)) == 6)
assert(toInt(DIV(THREE)(TWO)) == 1)
assert(toInt(DIV(THREE)(ONE)) == 3)
assert(toInt(MOD(FIVE)(THREE)) == 2)
assert(toInt(MOD(THREE)(ONE)) == 0)

assert(toBoolean(IS_ZERO(ZERO)))
assert(toBoolean(IS_ZERO(ONE))   == false)
assert(toBoolean(IS_ZERO(TWO))   == false)
assert(toBoolean(IS_ZERO(THREE)) == false)

assert(toBoolean(IS_LESS_EQUAL(ONE)(TWO)))
assert(toBoolean(IS_LESS_EQUAL(TWO)(TWO)))
assert(toBoolean(IS_LESS_EQUAL(THREE)(TWO)) == false)

assert(toBoolean(IS_EQUAL(ONE)(TWO))   == false)
assert(toBoolean(IS_EQUAL(TWO)(TWO)))
assert(toBoolean(IS_EQUAL(THREE)(TWO)) == false)

assert(toBoolean(IS_LESS_THAN(ONE)(TWO)))
assert(toBoolean(IS_LESS_THAN(TWO)(TWO))   == false)
assert(toBoolean(IS_LESS_THAN(THREE)(TWO)) == false)

//真偽値
assert(toBoolean(TRUE))
assert(toBoolean(FALSE) == false)
assert(toInt(IF(TRUE)(ONE)(TWO))  == 1)
assert(toInt(IF(FALSE)(ONE)(TWO)) == 2)

assert(toBoolean(NOT(TRUE)) == false)
assert(toBoolean(NOT(FALSE)))

assert(toBoolean(AND(TRUE)(TRUE)))
assert(toBoolean(AND(TRUE)(FALSE))  == false)
assert(toBoolean(AND(FALSE)(TRUE))  == false)
assert(toBoolean(AND(FALSE)(FALSE)) == false)

//ペア
def p1 = PAIR(ONE)(TWO)
assert(toInt(LEFT(p1))  == 1)
assert(toInt(RIGHT(p1)) == 2)

//リスト
def l1 = CONS(ONE)(CONS(TWO)(CONS(THREE)(NIL)))
def l2 = CONS(FOUR)(CONS(FIVE)(NIL))
assert(toList(l1, { toInt(it) }) == [1, 2, 3 ])
assert(toList(APPEND(l1)(l2), { toInt(it) }) == [1, 2, 3, 4, 5 ])
assert(toList(FLAT_MAP({ x -> CONS(x)(CONS(x)(NIL)) })(l1), { toInt(it) }) == [1, 1, 2, 2, 3, 3 ])
assert(toInt(FOLDL(ZERO)(ADD)(l1)) == 6)


//assert全部通ったら喜んどく
println '･:*+.\\(( °ω° ))/.:+'
