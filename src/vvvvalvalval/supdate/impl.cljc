(ns vvvvalvalval.supdate.impl)

(def upd*
  "A slightly modified version of update.
  If transient map `tm` contains key `k`, will add
  (f (get m k)) to the transient map `tm`."
  (let [not-found #?(:clj (Object.) :cljs (js-obj))]
    (fn upd*
      [m k f]
      (let [v (get m k not-found)]
        (if (identical? v not-found)
          m
          (assoc m k (f v))))
      )))

(def upd-dynamic*
  "A slightly modified version of update.
  If transient map `tm` contains key `k`, will add
  (f (get m k)) to the transient map `tm`."
  (let [not-found #?(:clj (Object.) :cljs (js-obj))]
    (fn upd-dynamic*
      [m k f]
      (let [v (get m k not-found)]
        (if (identical? v not-found)
          m
          (if (false? f)
            (dissoc m k)
            (assoc m k (f v)))))
      )))

(defn supd-map*
  [f coll]
  (if (vector? coll)
    (mapv f coll)
    (map f coll)))

(defn comp1
  "ad-hoc composition of 1-arity fns, faster than clojure.core/comp."
  ([] identity)
  ([f] f)
  ([a b]
   (fn [x]
     (-> x a b)
     ))
  ([a b c]
   (fn [x]
     (-> x a b c)
     ))
  ([a b c d]
   (fn [x]
     (-> x a b c d)
     ))
  ([a b c d e]
   (fn [x]
     (-> x a b c d e)
     ))
  ([a b c d e f]
   (fn [x]
     (-> x a b c d e f)
     ))
  ([a b c d e f g]
   (fn [x]
     (-> x a b c d e f g)
     ))
  ([a b c d e f g h]
   (fn [x]
     (-> x a b c d e f g h)
     ))
  ([a b c d e f g h i & rest]
   (let [fs (into [a b c d e f g h i ] rest)]
     (loop [fs fs]
       (if (-> fs count (> 1))
         (recur (->> fs (partition-all 8) (mapv #(apply comp1 %))))
         (first fs))))
    ))
