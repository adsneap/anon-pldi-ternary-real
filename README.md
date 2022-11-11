# Towards an API for Searchable Constructive Reals

## Java API

This is an initial Java implementation of our work with ternary Boehm reals.

Three uses are explored: performing arbitrary precision arithmetic, equation
solving via search and function minimisation via search. To see these examples
in action, see the file [`Examples`](java/Examples.java). See the paper and the
table at the bottom of this README for a description of each of the examples.

You can also feel free to write your own tests, based on those already there. 

### Files

Dyadic rationals are defined using `BigInteger` codes and `int` precision-levels
in the class [`DyadicCode`](java/DyadicCode.java). Negation, addition and
multiplication of dyadics are given, along with absolute value, minimum and
maximum functions. The function `toString()` displays the decimal
representation of a dyadic code.

The class [`VariableIntervalCode`](java/VariableIntervalCode.java) defines
variable-width codes `(k,c,p) : Iv`, consisting of two `BigInteger` codes
corresponding to `k` and `c`, and some `int` precision-level corresponding to
`p`. The class provides methods to negate, add and multiply codes, and a
conversion from `VariableIntervalCode`s to `SpecificIntervalCode`s. The class
[`SpecificIntervalCode`](java/SpecificIntervalCode.java) defines objects which
are simply instances of `VariableIntervalCode` with the `c := k+2`.

The [`FunctionCode`](java/FunctionCode.java) class implements multivariable
functions. A `FunctionCode` object contains its `int` arity, an approximator
function and a continuity oracle. The approximator function has type
`Function<List<VariableIntervalCode>,VariableIntervalCode>`; the formalisation
of this work gives full confidence that these approximator functions correspond
to the functions which we wish to realise. The continuity oracle has type
`BiFunction<TernaryBoehmReal,Integer,List<Integer>>`, which tells us to which
level we must search our input to be able to find the output at the requested
precision-level. To be able to construct polynomial functions, we need to be
able to negate, add, multiply and compose `FunctionCode`s -- all of these
operations are provided in the class. 

Uniformly continuous predicates are defined as
[`PredicateCode`](java/PredicateCode.java) objects. Each object contains the
`Function<TernaryBoehmReal,Boolean>` predicate `p` and its `int` modulus of
uniform continuity `delta`. Various predicates are defined statically, such as
one that checks whether a given `TernaryBoehmReal y` is within distance
`1/2^epsilon` of the search candidate for any `int epsilon`. Any
`PredicateCode p` can be defined in terms of a `FunctionCode f` to give a new
`PredicateCode (p . f)` using the method `lift`.

The [`TernaryBoehmReal`](java/TernaryBoehmReal.java) class defines real
encodings in `𝕋`, and can generate such encodings from `VariableIntervalCode`s
and `SpecificIntervalCode`s. These may be printed as intervals for given
precision-levels using `toString(int n)`. From the `FunctionCode` encoded
operations, we receive operations for absolute value, negation, addition,
multiplication and positive-integer exponentiation. A ternary Boehm real that
is produced as a result of an arithmetic operation is guaranteed to be correct
up to every degree of precision -- the output is recompiled for each higher
level of precision requested. One that is produced as a result of a search is
guaranteed to be correct up to the requested precision-level of the search.

Finally, the class [`Searchers`](java/Searchers.java) provides three ways to
search `PredicateCode`s. There are implementations of the naive, heuristic-based
and branch-and-bound algorithms from the previous section, all of which
terminate and return an answer if one exists in the given compact interval.
Since we are using an imperative language, we can choose to return an error if
a search fails. This is applicable for situations where solution is not
guaranteed, for example finding a root of a function in a compact interval.
Note that the heuristic-based algorithm requires one to provide those heuristics
as input.

### Comparison to other libraries

There are many, much more advanced, libraries for constructive real arithmetic,
such as `iRRAM`.

However, we compare our library to two more straightforward implementations:
Boehm's original constructive real library (circa 199?) and Martin Escardo's
Haskell implementation of exact real arithmetic on the interval [-1,1].
We perform these comparisons in order to ascertain whether or not our
implementation is in good company (as a toy implementation) in terms of
efficiency. We find that we are able to perform artihmetic, equation solving
and function optimisation correctly *and* efficiently.

* To compile the `Examples.hs file`, you must download and import Escardo's
reals file: https://www.cs.bham.ac.uk/~mhe/.talks/phdopen2013/realreals.lhs
* To compile the `ExamplesBoehm.java` file, you must download and import two
files from Boehm's CR Java API: https://www.hboehm.info/new_crcalc/CRCalc.html.
The files are CR.java and UnaryCRFunction.java.

## Agda Formalisation

This is a work-in-progress Agda formalisation of the mathematical
content in the paper *Towards an API for Searchable Real Numbers*
submitted to *PLDI 2023*.

Much, but not all, of the content of the paper is formalised here
in constructive type theory, in order to provide a computational
bridge between our 'on paper' mathematical work and our Java API.

This avenue of work is nascent, but the idea is that, ultimately,
everything computable in the Java API will derive directly from
constructive proofs in this Agda formalisation. Of course, the
derived Agda programs will be (much) slower than the Java code,
but they will show that our search algorithms on ternary Boehm
encodings are directly related to the underlying mathematics
taking place on the real numbers.

The formalisation library is built in Martin Escardo's Agda
library `TypeTopology`, which formalises a huge portion of
mathematics in constructive, univalent type theory. Its approach
is compatible with that of the HoTT book.

### Installation

Each main file in this library is a literate Agda file that
provides commentary on the definitions and proofs being
formalised. Therefore, we simply recommend that one views the
files in the GitHub browser, which provides a nice interface
for both the code and markdown.

If one prefers, then one should download Escardo's `TypeTopology`
library at commit ???, and place the files of this repo in a
new folder `source/PLDI/`. The files can then be compiled using
Agda; the instructions of installing which can be found online.

### Overview and relationship to paper

Our paper's supplementary Java and Agda code were written in
parallel and, thus, they are very alike. For example, the
machinery to lift functions is virtually the same in both
languages -- it simply has to be formalised in Agda, rather than
"assumed to work" as in Java. This alikeness is intended, as we
wish the Agda library to be seen as a formalisation of the Java
API.

#### Main files

[`TernaryBoehmReals`](agda/1-TernaryBoehmReals.lagda.md) defines the
type of ternary Boehm reals that the paper/API uses for searching
real numbers. Chiefly, we define encodings of reals in compact
intervals and prove a rather tedious lemma that shows we can
build an encoding that goes directly through two given
specific-width encodings of compact intervals.

[`FunctionEncodings`](agda/2-FunctionEncodings.lagda.md) then defines
types for sequences on variable-width and specific-width interval
encodings as in the paper's Sections 3 and 6. We show that these
sequence encodings can be used to define encode real numbers,
and show how the ternary Boehm reals also arise from the same
principles. Importantly, we provide machinery that allows the
lifting of approximated functions on the mathematical real space
to functions on ternary Boehm reals. We provide negation as an
example, though the Java API has more examples.

[`TernaryBoehmRealsSearch`](agda/3-TernaryBoehmRealsSearch.lagda.md)
combines the two files above to formalise how ternary Boehm
encodings can be used to search encodings of functions on the
mathematical real space. It gives the definition of a searchable
type and shows that uniform predicates on compact intervals of
ternary Boehm encodings are searchable. Then, it shows the idea
of how functions encoded using the aforementioned machinery can
be used to provide searchable predicates based on the codomain 
of these functions.

#### Supplementary files

[`Prelude`](Prelude.lagda.md) includes a variety of definitions
and lemmas that are needed for our work but are not in
`TypeTopology'. This includes extra lemmas on the integer
ordering and on vectors.

[`DyadicRationals`](DyadicRationals.lagda.md) defines the type of
dyadic rational numbers -- i.e. those rationals of form `k/2^i`
where `k, i : ℤ`.

[`DyadicReals`](DyadicReals.lagda.md) defines the type of
Dedekind real numbers using dyadic rational numbers. This is used
as our type of 'mathematical' real numbers throughout the
library.

[`BelowAndAbove`](BelowAndAbove.lagda.md) consists of a bunch of
lemmas needed for the structural operations on the ternary Boehm
encodings.

[`upValue`](upValue.lagda.md) defined the operation `upValue`,
which is based on `log₂` and used in our function machinery.

### Future work

We have chosen to leave the dyadic rationals, and many lemmas
related to them, unformalised. We also do not directly define
any operations on the dedekind real numbers; instead, we
assume their existence and the existence of the expected
properties (for example, commutativity of addition). This is
because this work would be time consuming and is not a
contribution of our paper. However, formalising this part of
the library would be an interesting Agda project in and of
itself, and so it is a long-term goal for our project to
fully formalise these lemmas.

`FunctionMachinery` only gives negation as a completed example.
In the future, we will provide projection, addition,
multiplication and composition as further examples.

`TernaryBoehmRealsSearch` is incomplete. The fact that any
function on ternary Boehm encodings with a continuity oracle
yields a uniform continuity oracle that can be used to search
that function (Lemma 6.28 in the paper) has been assumed. This
is because we prioritised formalising other results over this
extensive, but clearly true, lemma. The formalistaion of this
lemma is a priority for future work.

## Appendix: Table of examples/comparisons

| Group                           | # | Experiment Description                                      | Precision | Ternary Boehm Method  in Examples.java | Boehm 1990s Method  in ExamplesBoehm.java | Escardo Signed-Digit Method in Examples.hs |
|---------------------------------|---|-------------------------------------------------------------|-----------|----------------------------------------|-------------------------------------------|--------------------------------------------|
| Arbitrary Precision Computation | 1 | Compute 0.1                                                 | 2^-50     | arbitrary_ex1()                        | arbitrary_ex1_b()                         | arbitrary_ex1_e                            |
|                                 | 2 | Compute 1000000 * 2^-50                                     | 2^-50     | arbitrary_ex2()                        | arbitrary_ex2_b()                         |                                            |
|                                 | 3 | Compute 8x^10 - 6x^3 - 4x^2 where x = -10                   | 2^-50     | arbitrary_ex3()                        | arbitrary_ex3_b()                         |                                            |
|                                 | 4 | Compute 8x^10 - 6x^3 - 4x^2 where x = 0                     | 2^-50     | arbitrary_ex4()                        | arbitrary_ex4_b()                         |                                            |
|                                 | 5 | Compute 8x^10 - 6x^3 - 4x^2 where x = 99.5                  | 2^-50     | arbitrary_ex5()                        | arbitrary_ex5_b()                         |                                            |
| Equation Solve                  | 1 | Solve 1/2 = x * 1/2 in [-1,1] using  naive search           | 2^-20     | equation_ex1()                         |                                           | equation_ex1_e                             |
|                                 | 2 | Solve 1/2 = x * 1/2 in [-1,1] using  heuristic-based search | 2^-20     | equation_ex2()                         |                                           | equation_ex2_e                             |
|                                 | 3 | Solve 1/2 = x * 1/2 in [-1,1] using  heuristic-based search | 2^-500    | equation_ex3()                         |                                           | equation_ex3_e                             |
| Minimum Argument                | 1 | Minimise x^2 in [-1,1]                                      | 2^-20     | minimise_ex1()                         |                                           | minimise_ex1_e                             |
|                                 | 2 | Minimise x^2 in [-1,1]                                      | 2^-1000   | minimise_ex2()                         |                                           | minimise_ex2_e                             |
|                                 | 3 | Minimise 8x^10 - 6x^3 - 4x^2 in [-4,4]                      | 2^-20     | minimise_ex3()                         |                                           |                                            |
