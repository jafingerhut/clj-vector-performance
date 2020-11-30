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
    ;; It is important when using Criterium to measure the performance
    ;; of (= v1 v2) that the values of v1 and v2 have been initialized
    ;; outside of the expression given to crit/benchmark, otherwise
    ;; their construction will be included in the measurement.
    (crit/with-progress-reporting
      (crit/benchmark (= v1 v2) crit-opts))))

(defn perf-=-vec-and-vec-ret-true []
  (sweep-params =-vec-and-vec-ret-true
                [[1] [2] [4] [8] [16] [32] [64]
                 [128] [256] [512] [1024] [2048]
                 [4096] [8192] [16384] [32768] [65536]
                 [100000]]))

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

)
