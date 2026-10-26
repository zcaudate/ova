(ns ova-documentation.api
  (:require [ova.core :as ova]
            [ova.select :as select])
  (:use code.test))

[[:chapter {:title "ova.core"}]]

"`ova-new` creates an empty Ova. `ova-item-push` appends a value; `ova-item-pop` removes the last value and does nothing when empty; `ova-item-update` applies a function at an index and does nothing when the index is absent. All three mutation functions return the Ova."

(fact "indexed mutation and transient operations"
  (let [ov (ova/ova-new)]
    (conj! ov 1)
    (conj! ov 2)
    (ova/ova-item-update ov 0 inc)
    (assoc! ov 1 10)
    (pop! ov)
    [(persistent! ov) (nth ov 9 :missing)])
  => [[2] :missing])

"`ova-watch-add` takes `[ov key type handler]`; `ova-watch-remove` takes `[ov key]`. They return the updated watch map. The protocol equivalents are `-add-watch` and `-remove-watch`. `-push-item` and `-pop-item` provide the mutation protocol."

(fact "update events expose old and new values"
  (let [ov (ova/ova-new)
        events (atom [])]
    (ova/ova-item-push ov 2)
    (ova/-add-watch ov :scores :ova/update
                    (fn [_ event]
                      (swap! events conj
                             (select-keys event [:index :old-value :new-value]))))
    (ova/ova-item-update ov 0 #(+ % 10))
    (ova/-remove-watch ov :scores)
    @events)
  => [{:index 0 :old-value 2 :new-value 12}])

[[:chapter {:title "ova.select"}]]

"`ova-select [ov pred]` returns matching values. `ova-update-where [ov pred f]` updates matching values and emits update events. `ova-remove-where [ov pred]` removes matching values, rebuilds positions, and emits pop events using original indices."

(fact "predicate selection preserves duplicates"
  (let [ov (ova/ova-new)]
    (doseq [value [1 2 2 3]] (ova/ova-item-push ov value))
    (select/ova-select ov even?))
  => [2 2])

"`ova-index-of [ov container]` returns an element container's current index, or nil if absent. It takes the atom from `(:data ov)` or an event's `:container`, rather than the element value. Container identity is preserved by updates and by removal of other elements."

(fact "container positions follow removal"
  (let [ov (ova/ova-new)]
    (doseq [value [1 2 3]] (ova/ova-item-push ov value))
    (let [container (nth @(:data ov) 2)]
      (select/ova-remove-where ov even?)
      [(select/ova-index-of ov container) @ov]))
  => [1 [1 3]])
