;;;; benchmarks
(require '[vvvvalvalval.supdate.api :as s])
(require '[criterium.core :as bench])

(println "statically compiled:")
(let [v {:a 1
         :b {"c" [{:d 1 :e 1} {:d 2 :e 2} {:d 3 :e 3}]}
         :c [1 2 3]
         :d 1
         :e {:f 1}
         :g "dissoc"
         :h :ignore}]
  (bench/bench
    (s/supdate
      v
      {:a inc
       :b {"c" [{:d inc}]}
       :c [inc]
       :d [inc inc inc]
       :e :f
       :g false})))

;Evaluation count : 36046860 in 60 samples of 600781 calls.
;Execution time mean : 1.863463 µs
;Execution time std-deviation : 173.187401 ns
;Execution time lower quantile : 1.612410 µs ( 2.5%)
;Execution time upper quantile : 2.263820 µs (97.5%)
;Overhead used : 1.787988 ns

(println "fully dynamic:")
(let [v {:a 1
         :b {"c" [{:d 1 :e 1} {:d 2 :e 2} {:d 3 :e 3}]}
         :c [1 2 3]
         :d 1
         :e {:f 1}
         :g "dissoc"
         :h :ignore}]
  (bench/bench
    (s/supdate*
      v
      {:a inc
       :b {"c" [{:d inc}]}
       :c [inc]
       :d [inc inc inc]
       :e :f
       :g false})))
;Evaluation count : 13316220 in 60 samples of 221937 calls.
;Execution time mean : 4.507757 µs
;Execution time std-deviation : 461.057233 ns
;Execution time lower quantile : 3.772157 µs ( 2.5%)
;Execution time upper quantile : 5.300140 µs (97.5%)
;Overhead used : 1.787988 ns

(println "dynamic, pre-allocated transform:")
(let [v {:a 1
         :b {"c" [{:d 1 :e 1} {:d 2 :e 2} {:d 3 :e 3}]}
         :c [1 2 3]
         :d 1
         :e {:f 1}
         :g "dissoc"
         :h :ignore}
      t {:a inc
         :b {"c" [{:d inc}]}
         :c [inc]
         :d [inc inc inc]
         :e :f
         :g false}]
  (bench/bench
    (s/supdate*
      v
      t)))
;Evaluation count : 15684840 in 60 samples of 261414 calls.
;Execution time mean : 4.360580 µs
;Execution time std-deviation : 384.968857 ns
;Execution time lower quantile : 3.687479 µs ( 2.5%)
;Execution time upper quantile : 5.125703 µs (97.5%)
;Overhead used : 1.787988 ns


(println "dynamically compiled:")
(let [v {:a 1
         :b {"c" [{:d 1 :e 1} {:d 2 :e 2} {:d 3 :e 3}]}
         :c [1 2 3]
         :d 1
         :e {:f 1}
         :g "dissoc"
         :h :ignore}
      f (s/compile {:a inc
                    :b {"c" [{:d inc}]}
                    :c [inc]
                    :d [inc inc inc]
                    :e :f
                    :g false})]
  (bench/bench
    (f v)))
;Evaluation count : 36327720 in 60 samples of 605462 calls.
;Execution time mean : 1.993711 µs
;Execution time std-deviation : 207.659740 ns
;Execution time lower quantile : 1.632329 µs ( 2.5%)
;Execution time upper quantile : 2.408715 µs (97.5%)
;Overhead used : 1.787988 ns

;; ------------------------------------------------------------------------------
;; Observations
;; 1. it seems the overhead of allocating the dynamic data structure can be neglected
;; 2. dynamic compilation seems to be fastest.