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
