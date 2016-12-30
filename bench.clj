;;;; benchmarks
(require '[vvvvalvalval.supdate.api :as s])
(require '[criterium.core :as bench])
(require '[vvvvalvalval.supdate.impl :as impl])

(def ^:dynamic *quick-bench* true)

(defmacro bench
  [expr]
  `(if *quick-bench*
     (bench/quick-bench ~expr)
     (bench/bench ~expr)))

(defmacro quick-bench-then-bench
  [& body]
  `(do
     (println "*** quick bench ***")
     (binding [*quick-bench* true] ~@body)
     (println "*** full bench ***")
     (binding [*quick-bench* false] ~@body)))

(quick-bench-then-bench

  (println "hand-written")
  (let [v {:a 1
           :b {"c" [{:d 1 :e 1} {:d 2 :e 2} {:d 3 :e 3}]}
           :c [1 2 3]
           :d 1
           :e {:f 1}
           :g "dissoc"
           :h :ignore}]
    (bench
      (-> v
        (impl/upd* :a inc)
        (impl/upd* :b
          (fn [v]
            (impl/upd* v "c"
              (fn [v]
                (mapv (fn [v] (impl/upd* v :d inc)) v)))))
        (impl/upd* :c #(mapv inc %))
        (impl/upd* :d #(-> % inc inc inc))
        (impl/upd* :e :f)
        (dissoc :g))))
  ;Evaluation count : 507264 in 6 samples of 84544 calls.
  ;Execution time mean : 1.374814 µs
  ;Execution time std-deviation : 216.208337 ns
  ;Execution time lower quantile : 1.234110 µs ( 2.5%)
  ;Execution time upper quantile : 1.725975 µs (97.5%)
  ;Overhead used : 1.992165 ns

  (println "statically compiled:")
  (let [v {:a 1
           :b {"c" [{:d 1 :e 1} {:d 2 :e 2} {:d 3 :e 3}]}
           :c [1 2 3]
           :d 1
           :e {:f 1}
           :g "dissoc"
           :h :ignore}]
    (bench
      (s/supdate
        v
        {:a inc
         :b {"c" [{:d inc}]}
         :c [inc]
         :d [inc inc inc]
         :e :f
         :g false})))
  ;Evaluation count : 354186 in 6 samples of 59031 calls.
  ;Execution time mean : 1.654759 µs
  ;Execution time std-deviation : 70.492678 ns
  ;Execution time lower quantile : 1.586715 µs ( 2.5%)
  ;Execution time upper quantile : 1.733971 µs (97.5%)
  ;Overhead used : 1.992165 ns

  (println "statically compiled + hinted:")
  (let [v {:a 1
           :b {"c" [{:d 1 :e 1} {:d 2 :e 2} {:d 3 :e 3}]}
           :c [1 2 3]
           :d 1
           :e {:f 1}
           :g "dissoc"
           :h :ignore}]
    (bench
      (s/supdate
        v
        {:a ^{::s/type :fn} inc
         :b {"c" [{:d ^{::s/type :fn} inc}]}
         :c [^{::s/type :fn} inc]
         :d [^{::s/type :fn} inc ^{::s/type :fn} inc ^{::s/type :fn} inc]
         :e :f
         :g false})))
  ;Evaluation count : 347934 in 6 samples of 57989 calls.
  ;Execution time mean : 1.616539 µs
  ;Execution time std-deviation : 165.752649 ns
  ;Execution time lower quantile : 1.411931 µs ( 2.5%)
  ;Execution time upper quantile : 1.827650 µs (97.5%)
  ;Overhead used : 1.992165 ns

  (println "fully dynamic:")
  (let [v {:a 1
           :b {"c" [{:d 1 :e 1} {:d 2 :e 2} {:d 3 :e 3}]}
           :c [1 2 3]
           :d 1
           :e {:f 1}
           :g "dissoc"
           :h :ignore}]
    (bench
      (s/supdate*
        v
        {:a inc
         :b {"c" [{:d inc}]}
         :c [inc]
         :d [inc inc inc]
         :e :f
         :g false})))
  ;Evaluation count : 151218 in 6 samples of 25203 calls.
  ;Execution time mean : 4.163147 µs
  ;Execution time std-deviation : 824.923638 ns
  ;Execution time lower quantile : 3.412086 µs ( 2.5%)
  ;Execution time upper quantile : 5.165865 µs (97.5%)
  ;Overhead used : 1.992165 ns

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
    (bench
      (f v)))
  ;Evaluation count : 37394880 in 60 samples of 623248 calls.
  ;Execution time mean : 1.887358 µs
  ;Execution time std-deviation : 230.147780 ns
  ;Execution time lower quantile : 1.529777 µs ( 2.5%)
  ;Execution time upper quantile : 2.272998 µs (97.5%)
  ;Overhead used : 1.992165 ns

  )

;;Benchmarks run on:
;MacBook Air (11-inch, Early 2015)
;Processor 2,2 GHz Intel Core i7
;Memory 8 GB 1600 MHz DDR3
;Mac OS X Yosemite 10.10.5 (14F27)
;Java HotSpot(TM) 64-Bit Server VM 1.8.0_05-b13
