(ns vvvvalvalval.supdate.api
  (:require [vvvvalvalval.supdate.impl :as impl])
  (:refer-clojure :exclude [compile]))

(defn supdate*
  "Dynamic counterpart to the `supdate` macro, which works by using runtime type checks."
  [v transform]
  (cond
    (fn? transform)
    (transform v)

    (map? transform)
    (when v
      (persistent!
        (reduce-kv
          (fn [tm k spec]
            (if (false? spec)
              (dissoc! tm k)
              (impl/upd!* v tm k #(supdate* % spec))))
          (transient v) transform)))

    (and (vector? transform) (= (count transform) 1))
    (let [sspec (first transform)]
      (impl/supd-map* #(supdate* % sspec) v))

    (sequential? transform)
    (reduce supdate* v transform)

    (keyword? transform)
    (transform v)

    :else
    (throw (ex-info "Unrecognized transform."
             {:v v :transform transform}))
    ))

#?(:clj
   (defn- static-transform?
     [t-form]
     (or
       (map? t-form) (vector? t-form) (false? t-form) (keyword? t-form)
       (-> t-form (meta) (contains? ::type))))
   )

#?(:clj
   (defn- static-key?
     [key-form]
     (or (keyword? key-form) (string? key-form) (number? key-form) (#{true false} key-form)))
   )

#?(:clj
(defmacro supdate
  "'Super Update' - transforms an input based on a recursive, data-oriented specification which matches the schema of the input.

Accepts an input value `v` and a transform specification `transform` that represents a transformation to apply on v:
* if `transform` is a function (as determined by clojure.core/fn?) or keyword, will apply it to v.
To avoid the cost of a runtime type check, the caller may add the ^{:vvvvalvalval.supdate.api/type :fn} metadata to the `transform` form.
* if `transform` is a map, will treat v as a map, and recursively modify the values of v for the keys transform supplies.
The transform will only be performed for the keys that are contained in v.
If the transform value for a key is `false`, then the key is dissoc'ed from v.
* if `transform` is a vector with one element (a nested transform), will treat v as a collection an apply the nested transform to each element.
It the source collection is a vector, the output collection will be a vector as well.
* if transform is a sequence, will apply each transform in the sequence in order.

In order to achieve efficiency, this macro will attempt to leverage static information on the `transform` form,
thus generating code which skips type checks and dynamic traversal of the transform data structure at runtime."
  [v transform]
  (let [vsym (gensym "v")]
    `(let [~vsym ~v]
       ~(cond
          (map? transform)
          (let [tvsym (gensym "tv")]
            `(when ~vsym
               (as-> (transient ~vsym) ~tvsym
                 ~@(for [[k trf] transform]
                     (let [sk? (static-key? k)
                           ksym (if sk? k (gensym "k"))
                           form (cond
                                  (false? trf)
                                  `(dissoc! ~tvsym ~ksym)

                                  (static-transform? trf)
                                  `(impl/upd!* ~vsym ~tvsym ~ksym (fn [v#] (supdate v# ~trf)))

                                  :dynamic
                                  `(let [spec# ~trf]
                                     (impl/upd-dynamic!* ~vsym ~tvsym ~ksym (fn [v#] (supdate* v# spec#)))))]
                       (if sk? form `(let [~ksym ~k] ~form))))
                 (persistent! ~tvsym))))

          (keyword? transform)
          `(~transform ~vsym)

          (and (vector? transform) (= (count transform) 1))
          `(impl/supd-map* (fn [e#] (supdate e# ~(first transform))) ~vsym)

          (and (vector? transform) (> (count transform) 1))
          `(as-> ~vsym ~vsym
             ~@(for [spec transform]
                 `(supdate ~vsym ~spec)))

          (-> transform (meta) (get ::type) (= :fn))
          `(~transform ~vsym)

          :else
          `(supdate* ~vsym ~transform)
          ))))
)

(defn compile
  "Given a transform specification (as passed as second argument to supdate or supdate*),
  returns a function which accepts an input and transforms it.
  The returned function runs faster than just calling supdate* (i.e interpreting the transform),
  at the expense of more ahead-of-time work."
  [transform]
  (cond
    (fn? transform)
    transform

    (keyword? transform)
    #(get % transform)

    (map? transform)
    (apply impl/comp2
      (for [[k trf] transform]
        (if (false? trf)
          (fn [m tm]
            (dissoc! tm k))
          (let [f (compile trf)]
            (fn [m tm]
              (impl/upd!* m tm k f))))))

    (and (vector? transform) (= (count transform) 1))
    (let [f (compile (first transform))]
      (fn [v]
        (impl/supd-map* f v)))

    (sequential? transform)
    (let [fs (map compile transform)]
      ;; PERFORMANCE maybe a special-purpose 'comp1' fn would improve performance here. (Val, 29 Dec 2016)
      (apply comp (reverse fs)))

    :else
    (throw (ex-info "Unrecognized transform."
             {:transform transform}))
    ))
