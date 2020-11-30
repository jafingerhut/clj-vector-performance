(ns clj-vectors-perf.main
  (:require [criterium.core :as crit]))

(def crit-opts {:samples 30})

(defn sweep-params [f params-list]
  (let [params-vec (vec params-list)
        results-vec (mapv #(apply f %) params-list)]
    (dotimes [i (count params-vec)]
      (println)
      (println "params:" (params-vec i))
      (crit/report-result (results-vec i)))))

(defn =-vec-and-vec-ret-true [n]
  (let [v1 (vec (range n))
        v2 (vec (range n))]
    (crit/with-progress-reporting
      (crit/benchmark (= v1 v2) crit-opts))))

(defn perf-=-vec-and-vec-ret-true []
  (sweep-params =-vec-and-vec-ret-true
                [[1] [2]]))

(comment
    
(def v1 (vec (range 50)))
v1
(def v2 (vec (range 50)))
(= v1 v2)
;; debug print statements I added to Clojure 1.10.1 source code
;; Confirmed that PersistentVector.nth() is called once for each of
;; the two vectors, for each of indexes 0 thru 49.

;; It seems likely to be more efficient if we could ensure that this
;; operation uses the PersistentVector.iterator() implementation.

(defn compare-vec-size-n [n]
  (let [v1 (vec (range n))
        v2 (vec (range n))]
    (time (= v1 v2))))

(defn compare-vec-size-n [ntimes n]
  (let [v1 (vec (range n))
        v2 (vec (range n))]
    (frequencies (mapv (fn [_] (time (= v1 v2)))
                       (range ntimes)))))

(defn compare-vec-to-seq-size-n [ntimes n]
  (let [v (vec (range n))
        s (range n)]
    (frequencies (mapv (fn [_] (time (= v s)))
                       (range ntimes)))))

(defn compare-vec-size-n-neq [ntimes n]
  (let [v1 (vec (range n))
        v2 (vec (range n))
        v2 (assoc v2 (dec n) 0)]
    (frequencies (mapv (fn [_] (time (= v1 v2)))
                       (range ntimes)))))

(defn hash-vec-size-n [ntimes n]
  (let [vecs (mapv (fn [_] (vec (range n))) (range ntimes))]
    (frequencies (mapv (fn [v] (time (hash v)))
                       vecs))))

(compare-vec-size-n 50 50)
(compare-vec-size-n 1000)
(compare-vec-size-n 10000)
(compare-vec-size-n 50 1000000)
(compare-vec-size-n-neq 50 1000000)

(compare-vec-to-seq-size-n 1 20)

(hash-vec-size-n 50 1000000)

(println "first of 10")

;; With Clojure 1.10.1 modified with my changes to
;; APersistentVector.doEquiv that uses List iterator() method instead
;; of nth(), times ranged from:

"Elapsed time: 13.021462 msecs"
"Elapsed time: 13.322932 msecs"
"Elapsed time: 14.192369 msecs"
"Elapsed time: 15.139772 msecs"
"Elapsed time: 15.697675 msecs"
"Elapsed time: 15.830441 msecs"
"Elapsed time: 17.473316 msecs"
"Elapsed time: 19.406707 msecs"
"Elapsed time: 19.925345 msecs"
"Elapsed time: 19.994562 msecs"

;; with unmodified Clojure 1.10.1, times ranged from:

"Elapsed time: 20.749841 msecs"
"Elapsed time: 20.993374 msecs"
"Elapsed time: 21.082402 msecs"
"Elapsed time: 21.124058 msecs"
"Elapsed time: 21.707172 msecs"
"Elapsed time: 23.086769 msecs"
"Elapsed time: 23.955181 msecs"
"Elapsed time: 27.352486 msecs"
"Elapsed time: 34.673524 msecs"
"Elapsed time: 37.507745 msecs"

)
