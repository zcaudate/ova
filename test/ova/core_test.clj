(ns ova.core-test
  (:use code.test)
  (:require [ova.core :as v]
            [std.lib :as h]))

^{:refer ova.core/ova-watch-remove :added "1.0"}
(fact "removes a watch"

  (-> (doto {:watches (atom {})}
        (v/ova-watch-add  
         :test/a
         :ova/push
         (fn [key event])))
      (v/ova-watch-remove :test/a))
  => {})

^{:refer ova.core/ova-watch-add :added "1.0"}
(fact "adds a watch"

  (:test/a (v/ova-watch-add  {:watches (atom {})}
                             :test/a
                             :ova/push
                             (fn [key event])))
  => (contains {:type :ova/push,
                :handler fn?}))

^{:refer ova.core/ova-watch-trigger :added "1.0"}
(fact "triggers a watch"

  (let [output (atom nil)]
    (v/ova-watch-trigger
     {:watches (atom {:test/a {:handler (fn [key event]
                                          (reset! output {:key key
                                                          :event (dissoc (into {} event)
                                                                         :container)}))
                               :type :ova/push}})}
     (v/->OvaEvent :ova/push
                   (atom 1)
                   0
                   1
                   nil))
    @output)
  => {:key :test/a, :event {:type :ova/push, :index 0, :new-value 1, :old-value nil}})

^{:refer ova.core/ova-index-make :added "1.0"}
(fact "makes an array index"

  (v/ova-index-make ["a" "b" "c"])
  => {"a" 0, "b" 1, "c" 2}

  (->> (v/ova-index-make [(atom "A") (atom "B") (atom "C")])
       (h/map-keys deref))
  => {"A" 0, "B" 1, "C" 2})

^{:refer ova.core/ova-item-push :added "1.0"}
(fact "pushes an element into the ova"

  (let [output (atom [])
        ov     (v/ova-new)
        _      (v/ova-watch-add  ov
                                 :test/a
                                 :ova/push
                                 (fn [key event]
                                   (swap! output conj (:new-value event))))
        _      (v/ova-item-push ov 1)
        _      (v/ova-item-push ov 2)
        _      (v/ova-item-push ov 3)]
    @output)
  => [1 2 3])

^{:refer ova.core/ova-item-pop :added "1.0"}
(fact "pops an element from the ova"

  (let [output (atom [])
        ov     (v/ova-new)
        _      (reset! (:data ov) [(atom 1) (atom 2) (atom 3)])
        _      (v/ova-watch-add  ov
                                 :test/a
                                 :ova/pop
                                 (fn [key event]
                                               (swap! output conj (:old-value event))))
        _      (v/ova-item-pop ov)]
    [@output @ov])
  => [[3] [1 2]])

^{:refer ova.core/ova-item-update :added "1.0"}
(fact "updates an element in the ova"

  (let [output (atom [])
        ov     (v/ova-new)
        _      (reset! (:data ov) [(atom 1) (atom 2) (atom 3)])
        _      (v/ova-watch-add  ov
                                 :test/a
                                 :ova/update
                                 (fn [key event]
                                   (swap! output conj (:old-value event))))
        _      (v/ova-item-update ov 1 (fn [x] (+ x 10)))]
    [@output @ov])
  => [[2] [1 12 3]])

^{:refer ova.core/ova-new :added "1.0"}
(fact "creates a new ova"

  @(v/ova-new)
  => [])
