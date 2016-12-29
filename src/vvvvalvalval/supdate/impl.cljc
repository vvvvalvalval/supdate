(ns vvvvalvalval.supdate.impl)

(defn upd!*
  "A slightly modified version of update.
  If map `m` contains key `k`, will add
  (f (get m k)) to the transient map `tm`."
  ;; we have to pass the original (non transient) map tm,
  ;; because it's the only one that supports the `contains?` operation.
  [m tm k f]
  (let [v (get tm k)]
    (if v
      (assoc! tm k (f v))
      (if (contains? m k)
        (assoc! tm k (f v))
        tm))))

(defn upd-dynamic!*
  "A version of upd! where we're not sure f is a function"
  [m tm k f]
  (let [v (get tm k)]
    (if v
      (if (false? f)
        (dissoc! tm k)
        (assoc! tm k (f v)))
      (if (contains? m k)
        (if (false? f)
          (dissoc! tm k)
          (assoc! tm k (f v)))
        tm))))

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
  ([a b c d e & rest]
   (let [fs (into [a b c d e] rest)]
     (loop [fs fs]
       (if (-> fs count (> 1))
         (recur (->> fs (partition-all 4) (mapv #(apply comp1 %))))
         (first fs))))
    ))

(defn comp2
  "ad-hoc composition function for map transforms"
  ([]
   (fn [x a] x))
  ([f]
   (fn [x a]
     (-> x (f a))
     ))
  ([f g]
   (fn [x a]
     (-> x (f a) (g a))
     ))
  ([f g h]
   (fn [x a]
     (-> x (f a) (g a) (h a))
     ))
  ([f g h i]
   (fn [x a]
     (-> x (f a) (g a) (h a) (i a))
     ))
  ([f g h i j]
   (fn [x a]
     (-> x (f a) (g a) (h a) (i a) (j a))
     ))
  ([f g h i j k]
   (fn [x a]
     (-> x (f a) (g a) (h a) (i a) (j a) (k a))
     ))
  ([f g h i j k l]
   (fn [x a]
     (-> x (f a) (g a) (h a) (i a) (j a) (k a) (l a))
     ))
  ([f g h i j k l m]
   (fn [x a]
     (-> x (f a) (g a) (h a) (i a) (j a) (k a) (l a) (m a))
     ))
  ([f g h i j k l m n & rest]
   (let [fs (into [f g h i j k l m n] rest)]
     (loop [fs fs]
       (if (-> fs count (> 1))
         (recur (->> fs (partition-all 8) (mapv #(apply comp2 %))))
         (first fs)))
     )))
