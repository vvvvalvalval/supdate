(ns vvvvalvalval.supdate.test.api
  (:require [midje.sweet :refer :all]
            [clojure.test :as test :refer :all]

            [vvvvalvalval.supdate.api :as supd :refer [supdate]])
  (:use clojure.repl))

(fact "nominal example"
  (supdate
    {:a 1 "b" [{:c 2} {:c 3 :d 4}]} ;; the data structure to transform
    {:a inc "b" [{:c dec :d false}] :missing inc} ;; the transformation
    )
  => {:a 2, "b" [{:c 1} {:c 2}]}

  (fact "comparing various execution strategies"
    (=
      (supdate
        {:a 1 "b" [{:c 2} {:c 3 :d 4}]}
        {:a inc "b" [{:c dec :d false}] :missing inc})
      (let [t {:a inc "b" [{:c dec :d false}] :missing inc}]
        (supdate
          {:a 1 "b" [{:c 2} {:c 3 :d 4}]}
          t))
      (supd/supdate*
        {:a 1 "b" [{:c 2} {:c 3 :d 4}]}
        {:a inc "b" [{:c dec :d false}] :missing inc})
      ((supd/compile {:a inc "b" [{:c dec :d false}] :missing inc})
        {:a 1 "b" [{:c 2} {:c 3 :d 4}]})
      ) => true)
  )

(fact "if transform is a function: apply it to the value"
  (supdate 0 inc) => 1
  (supdate 0 (fn [v] (inc v))) => 1
  (fact "a type hint can be provided for efficiency."
    (supdate 0 ^{::supd/type :fn} inc) => 1)
  )


(fact "if transform is a map: recursively transform the value for the given keys."
  (supdate
    {:a 1 "b" 1 1 1}
    {:a inc "b" inc 1 inc})
  => {:a 2 "b" 2 1 2}
  (let [t {:a inc "b" inc 1 inc}]
    (supdate
      {:a 1 "b" 1 1 1}
      t)
    => {:a 2 "b" 2 1 2})
  (fact "if a key is missing, no transform is applied"
    (supdate {:a 1} {:a inc :b dec}) => {:a 2}
    (supdate {} {:a inc}) => {}
    (supdate nil {:a inc}) => nil
    )
  (fact "If a value for a key in `transform` is false, then the key is dissoc'ed form the input."
    (supdate {:a 1} {:a false}) => {}
    (supdate nil {:a false}) => nil
    (supdate {:a 1} {:b false}) => {:a 1})
  )

(fact "if it's a vector with one (transform) element: transform each item in the collection."
  (supdate [1 2 3 4] [inc])
  => [2 3 4 5]
  (let [t [inc]]
    (supdate [1 2 3 4] [inc]))
  => [2 3 4 5]

  (supdate (list 1 2 3 4) [inc])
  => '(2 3 4 5)

  (supdate [] [inc]) => []
  (supdate () [inc]) => ()
  )

;; you can nest transforms arbitrarily.
(fact "Transforms can be nested"
  (supdate
    {:a {:b [{:c 1}]}}
    {:a {:b [{:c inc}]}})
  => {:a {:b [{:c 2}]}})

(fact
  "a vector with 2 or more elements means 'chain the transforms'"
  (supdate 0 [inc inc inc])
  => 3
  (supdate 0 [])
  => 0
  )

(require '[vvvvalvalval.supdate.api :as supd :refer [supdate]])

(def my-data
  {:a 1
   :b [1 2 3]
   :c {"d" [{:e 1 :f 1} {:e 2 :f 2}]}
   :g 0
   :h 0})

(supdate
  my-data
  {:a inc
   :b [inc]
   :c {"d" [{:e inc}]}
   :g [inc inc inc]})
=> {:a 2,
    :b [2 3 4],
    :c {"d" [{:e 2, :f 1} {:e 3, :f 2}]}
    :g 3,
    :h 0}
