# supdate

Clojure's update with superpowers.

[![Clojars Project](https://img.shields.io/clojars/v/vvvvalvalval/supdate.svg)](https://clojars.org/vvvvalvalval/supdate)

This library provides a `supdate` macro which lets you transform Clojure data structures
declaratively, using a data-driven specification which structure matches the schema of the input. 

The benefit is that such specifications eliminate a lot of the boilerplate code involved
 when transforming nested data structures. 
 
In addition, `supdate` is a macro that leverages static information on a best-effort basis 
in order to make the performance comparable to hand-written code.
Dynamic pre-compilation (via `compile`) is also available to achieve better performance while remaining fully dynamic.

## Usage

```clojure
(require '[vvvvalvalval.supdate.api :as supd :refer [supdate]])

;; canonical example
(def my-input
  {:a 1
   :b [1 2 3]
   :c {"d" [{:e 1 :f 1} {:e 2 :f 2}]}
   :g 0
   :h 0
   :i 0})

(supdate
  my-input
  {:a inc
   :b [inc]
   :c {"d" [{:e inc}]}
   :g [inc inc inc]
   :my-missing-key inc
   :i false
   })
=> {:a 2,
    :b [2 3 4],
    :c {"d" [{:e 2, :f 1} {:e 3, :f 2}]}
    :g 3,
    :h 0}
```

See also the [tests](https://github.com/vvvvalvalval/supdate/blob/master/test/vvvvalvalval/supdate/test/api.clj)
 for more examples.

### Emulating standard library operations

`supdate` generalizes several functions of Clojure's standard library:

```clojure

;;;; Emulating clojure.core/update
(update {:a 1 :b 1} :a inc)
=> {:a 2 :b 1}
(supdate {:a 1 :b 1} {:a inc})
=> {:a 2 :b 1}

;;;; Emulating clojure.core/update-in
(update-in {:a {"b" [{:c 1}]}}
  [:a "b" 0 :c] inc)
=> {:a {"b" [{:c 2}]}}
(supdate {:a {"b" [{:c 1}]}}
  {:a {"b" {0 {:c inc}}}})
=> {:a {"b" [{:c 2}]}}
;; NOTE: unlike update-in, if a key is missing in the input, 
;; the transformation will be skipped instead of creating new maps.

;;;; Emulating clojure.core/map 
(map dec (range 10))
=> (-1 0 1 2 3 4 5 6 7 8)
(supdate (range 10) [dec])
=> (-1 0 1 2 3 4 5 6 7 8)
;; Note: unlike map, if the input is a vector, the output will also be a vector.

;;;; Emulating dissoc
(dissoc {:a 1 :b 2 :c 3}
  :a :b :d)
=> {:c 3}
(supdate {:a 1 :b 2 :c 3}
  {:a false :b false})
=> {:c 3}
```

### Pre-compiling transforms

A `compile` function is available to make execution faster:

```clojure

(def transform
  (supd/compile {:a inc
                 :b [inc]
                 :c {"d" [{:e inc}]}
                 :g [inc inc inc]
                 :missing-key inc
                 :i false
                 }))

(transform {:a 1
            :b [1 2 3]
            :c {"d" [{:e 1 :f 1} {:e 2 :f 2}]}
            :g 0
            :h 0
            :i 0})
=> {:a 2,
    :b [2 3 4],
    :c {"d" [{:e 2, :f 1} {:e 3, :f 2}]}
    :g 3,
    :h 0}

```

## Comparison to [Specter](https://github.com/nathanmarz/specter)

This library is in the same space as Specter, but we don't see it as a replacement to Specter.
We believe this library is useful in situations where using Specter is overkill.

More specifically:

* Specter's `transform` is useful for making one sophisticated transformation,
 whereas `supdate` is good at making many basic transformations at once.
* Specter provides a `select` operation, supdate is only about transformation.
* Arguably, supdate is easier to learn.
* Specter is extensible, supdate is not.

## License

Copyright © 2016 Valentin Waeselynck and contributors 

Distributed under the MIT License.
