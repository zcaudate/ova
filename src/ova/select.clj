(ns ova.select
  "Predicate selection and mutation of Ova values.

   Bulk mutations return the Ova and notify its watches. They are sequential
   operations, with the same concurrency limits as the core mutation functions."
  (:require [ova.core :as ova]))

(defn ova-select
  "Returns values matching pred in Ova order, preserving duplicates."
  [ov pred]
  (filterv pred @ov))

(defn ova-update-where
  "Applies f to each matching value and returns ov.
   Emits an :ova/update event for each updated element."
  [ov pred f]
  (let [indices (into []
                      (keep-indexed (fn [index value]
                                      (when (pred value) index)))
                      @ov)]
    (doseq [index indices]
      (ova/ova-item-update ov index f))
    ov))

(defn ova-remove-where
  "Removes matching values, rebuilds positions, and returns ov.
   Emits :ova/pop events in original order, using indices before removal."
  [{:keys [data positions] :as ov} pred]
  (let [removed (into []
                      (keep-indexed (fn [index container]
                                      (let [value @container]
                                        (when (pred value)
                                          [index container value]))))
                      @data)
        containers (set (map second removed))]
    (when (seq removed)
      (let [remaining (swap! data #(into [] (remove containers) %))]
        (reset! positions (ova/ova-index-make remaining)))
      (doseq [[index container value] removed]
        (ova/ova-watch-trigger ov (ova/->OvaEvent :ova/pop container index nil value))))
    ov))

(defn ova-index-of
  "Returns the current index of an element container, or nil if absent.
   Pass the container atom from (:data ov) or a watch event."
  [{:keys [positions]} container]
  (get @positions container))
