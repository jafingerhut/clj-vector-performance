# Introduction

Some performance measurements of operations on Clojure vectors,
intended to quantify performance changes that occur with some imagined
changes to the Clojure/Java code that implements vectors.


# Benchmarks

## `clojure.core/=` between a Clojure vector and some other object

All of the operations mentioned in sub-sections of this one are
implemented via a method named `doEquiv` in the class
`clojure.lang.APersistent`.


### `clojure.core/=` between a Clojure vector and another vector

These tests exercise the code in method `doEquiv` of
`APerisistent.java`, the first case `if (obj instanceof
IPersistentVector)`.

```clojure
(require '[clj-vectors-perf.main :as m]
         '[criterium.core :as crit])

(def r1 (crit/with-progress-reporting
          (crit/benchmark (m/=-vec-and-vec-ret-true 16) {:samples 30})))
(def r2 (crit/with-progress-reporting
          (crit/benchmark (m/=-vec-and-vec-ret-true 512) {:samples 30})))
(def r3 (crit/with-progress-reporting
          (crit/benchmark (m/=-vec-and-vec-ret-true 100000) {:samples 30})))
(crit/report-result r1 :verbose)
(crit/report-result r2 :verbose)
(crit/report-result r3 :verbose)
```

|     n   | 1.10.2-alpha4 unmodified | 1.10.2-alpha4 modified |
| ------- | ------------------------ | ---------------------- |
|      16 | 623.3  nsec | 707.5   nsec |
|      32 |             |   1.065 usec |
|      64 |             |   1.886 usec |
|     128 |             |   3.528 usec |
|     256 |             |   8.235 usec |
|     512 |  26.98 usec |  17.10  usec |
|    1024 |             |  35.32  usec |
|    2048 |             |  73.08  usec |
|    4096 |             | 146.74  usec |
|    8192 |             | 297.86  usec |
|   16384 |             | 609.73  usec |
|   32768 |             |   1.211 msec |
|   65536 |             |   2.456 msec |
| 100,000 |   5.01 msec |   3.86  msec |

Evidence that it exercises that code:

Used the following sequence of commands to build a modified version of
Clojure.

The checked out tag is the commit immediately after the tag for
clojure-1.10.2-alpha4.

```bash
$ git clone https://github.com/clojure/clojure
$ cd clojure
$ git checkout f47c139e20970ee0852166f48ee2a4626632b86e
$ patch -p1 < patches/doequiv-vec-to-vec-extra-debug.patch
$ mvn -Dmaven.test.skip=true install

[ The mvn command above installed the file
~/.m2/repository/org/clojure/clojure/1.10.2-master-SNAPSHOT/clojure-1.10.2-master-SNAPSHOT.jar
in my ~/.m2 directory. ]

$ clj -A:clj:socket:modified-clj
Clojure 1.10.2-master-SNAPSHOT

user=> *clojure-version*
{:major 1, :minor 10, :incremental 2, :qualifier "master", :interim true}

user=> (require '[clj-vectors-perf.main :as m])

user=> (m/=-vec-and-vec-ret-true 100000)
dbg APersistentVector.doEquiv count=100000
dbg APersistentVector.doEquiv vec-to-vec count=100000
true
```


### `clojure.core/=` between a Clojure vector and something that implements `java.util.List`, but not a vector

These tests exercise the code in method `doEquiv` of
`APerisistent.java`, the second case `if (obj instanceof List)`.

Evidence that it exercises that code: tbd

### `clojure.core/=` between a Clojure vector and something that implements `clojure.lang.Sequential`, but not a vector and not something that implements `java.util.List`

These tests exercise the code in method `doEquiv` of
`APerisistent.java`, the third case _after_ `if (!(obj instanceof
Sequential)) return false`, so the value `obj` does implement the
`Sequential` interface.

Evidence that it exercises that code: tbd


## Java `equals` between a Clojure vector and some other object

All of the operations mentioned in sub-sections of this one are
implemented via a method named `doEquals` in the class
`clojure.lang.APersistent`.


## `clojure.core/hash` on Clojure vector

Implemented in method `hasheq` of class
`clojure.lang.APersistentVector`.


## Java `hashCode` on Clojure vector

Implemented in method `hashCode` of class
`clojure.lang.APersistentVector`.


## Java `indexOf` on Clojure vector

Implemented in method `indexOf` of class
`clojure.lang.APersistentVector`.


## Java `lastIndexOf` on Clojure vector

Implemented in method `lastIndexOf` of class
`clojure.lang.APersistentVector`.

It seems like it might not be worth the effort to create a special
"iterate backwords through the vector" elements just for perhaps
speeding up this one method.


## Java `listIterator` on Clojure vector

Implemented in method `listIterator` of class
`clojure.lang.APersistentVector`.  It calls `nth` once on each
element, and might get a performance benefit from using `iterator`
instead, but note that it has a `previous` method that moves
backwards, too.


## Java `rangedIterator` on Clojure vector

Implemented in method `rangedIterator` of class
`clojure.lang.APersistentVector`.


## Java `iterator` on Clojure vector

Implemented in method `iterator` of class
`clojure.lang.APersistentVector`.


## Java `toArray` on Clojure vector

Implemented in method `toArray` of class
`clojure.lang.APersistentVector`.


## Java `compareTo` on Clojure vector

Implemented in method `compareTo` of class
`clojure.lang.APersistentVector`.


## Method `reduce` on Clojure vector in inner class `APersistentVector$Seq`

Implemented in two methods named `reduce` of class
`clojure.lang.APersistentVector$Seq`.

Note that the class `Seq` as a whole also calls `nth` inside its
method `first`, but since a separate `Seq` object instance is returned
from each call to the method `next`, and each `Seq` instance should be
immutable, there does not seem to be any real opportunity to use a
mutable iterator object except inside of the `reduce` method.


## Other methods in class `clojure.lang.APersistentVector` that call `nth`

These methods of class `clojure.lang.APersistentVector` also call
`nth`, but they only do so once, so there is no reason that using an
iterator would help speed them up.

+ `get`
+ `nth`
+ `invoke`
+ `peek`
+ `entryAt`
+ `valAt`
