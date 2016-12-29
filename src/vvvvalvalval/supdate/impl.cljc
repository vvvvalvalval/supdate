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

(defn comp2
  "ad-hoc composition function for map transforms"
  ([]
    (fn [v] v))
  ([f]
   (fn [v]
     (->> (transient v) (f v) persistent!)))
  ([f g]
   (fn [m]
     (->> (transient m) (f m) (g m) persistent!)))
  ([f g h]
   (fn [v]
     (->> (transient v)
       (f v) (g v) (h v)
       persistent!)))
  ([f g h i]
   (fn [v]
     (->> (transient v)
       (f v) (g v) (h v) (i v)
       persistent!)))
  ([f g h i j]
   (fn [v]
     (->> (transient v)
       (f v) (g v) (h v) (i v) (j v)
       persistent!)))
  ([f g h i j k]
   (fn [v]
     (->> (transient v)
       (f v) (g v) (h v) (i v) (j v) (k v)
       persistent!)))
  ([f g h i j k l]
   (fn [v]
     (->> (transient v)
       (f v) (g v) (h v) (i v) (j v) (k v) (l v)
       persistent!)))
  ([f g h i j k l m]
   (fn [v]
     (->> (transient v)
       (f v) (g v) (h v) (i v) (j v) (k v) (l v) (m v)
       persistent!)))
  ([f g h i j k l m n]
   (fn [v]
     (->> (transient v)
       (f v) (g v) (h v) (i v) (j v) (k v) (l v) (m v) (n v)
       persistent!)))
  ([f g h i j k l m n o]
   (fn [v]
     (->> (transient v)
       (f v) (g v) (h v) (i v) (j v) (k v) (l v) (m v) (n v) (o v)
       persistent!)))
  ([f g h i j k l m n o p & rest]
   (let [rest (vec rest)]
     (if (empty? rest)
       (fn [v]
         (->> (transient v)
           (f v) (g v) (h v) (i v) (j v) (k v) (l v) (m v) (n v) (o v) (p v)
           persistent!))
       (fn [v]
         (persistent!
           (reduce
             (fn [f tv]
               (f v tv))
             (->> (transient v)
               (f v) (g v) (h v) (i v) (j v) (k v) (l v) (m v) (n v) (o v) (p v))
             rest))
         ))))
  )