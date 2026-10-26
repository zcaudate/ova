(ns ova-documentation.guide
  (:require [ova.core :as ova]
            [ova.select :as select])
  (:use code.test))

[[:chapter {:title "Getting started"}]]

"Ova is a mutable array whose elements live in individual atoms. Use `ova.core` for construction, indexed mutation, and watches; use `ova.select` for predicate selection and bulk mutation."

"Create an empty Ova with `ova-new`, then append values with `ova-item-push` or `conj!`. Dereferencing produces a vector of values."

(fact "construct and read an Ova"
  (let [ov (ova/ova-new)]
    (conj! ov {:id :bill :score 10})
    (conj! ov {:id :sally :score 20})
    [(count ov) (nth ov 0) (mapv :score (seq ov)) @ov])
  => [2 {:id :bill :score 10} [10 20]
      [{:id :bill :score 10} {:id :sally :score 20}]])

[[:chapter {:title "Select and update"}]]

"Predicates receive element values. Selection returns a vector in array order, preserving duplicates. Bulk updates and removals return the original Ova."

(fact "update selected values"
  (let [ov (ova/ova-new)]
    (doseq [value [1 2 3 4]] (ova/ova-item-push ov value))
    (select/ova-update-where ov even? #(+ % 10))
    (select/ova-remove-where ov odd?)
    @ov)
  => [12 14])

[[:chapter {:title "Observe changes"}]]

"Register a watch with a unique key, an event type (`:ova/push`, `:ova/pop`, or `:ova/update`), and a function receiving the key and event. Events contain `:type`, `:container`, `:index`, `:new-value`, and `:old-value`. Remove watches by key. Changes made directly to element atoms bypass these events."

[[:chapter {:title "Mutation and concurrency"}]]

"The redesigned Ova uses atoms rather than refs. `dosync` does not make its operations transactional. Each atom swap is atomic, but bulk operations and changes to the array and position map are separate steps. Coordinate concurrent structural changes in application code."

"`seq` returns a sequence of captured values; `count` reports the number of elements; `nth` supports a default for missing indices. `persistent!` returns the current values as a vector. Sorting, insertion, and field-specific watches are not currently provided."
