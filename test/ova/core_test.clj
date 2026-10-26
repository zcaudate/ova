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

^{:refer ova.core/-remove-watch :added "1.0"}
(fact "removes a watch through the protocol using its key"
  (let [ov (v/ova-new)
        output (atom [])]
    (v/-add-watch ov :removed :ova/push
                  (fn [key _] (swap! output conj key)))
    (v/-add-watch ov :retained :ova/push
                  (fn [key _] (swap! output conj key)))
    (v/-remove-watch ov :removed)
    (v/-push-item ov 1)
    [@output (set (keys @(:watches ov)))])
  => [[:retained] #{:retained}])

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
  => [[3] [1 2]]

  (let [ov (v/ova-new)
        output (atom [])]
    (doseq [value [nil false 3]]
      (v/ova-item-push ov value))
    (let [containers @(:data ov)]
      (v/ova-watch-add ov :pop :ova/pop
                       (fn [_ event] (swap! output conj event)))
      (dotimes [_ 5] (v/ova-item-pop ov))
      (let [empty-state [@ov @(:positions ov) (vector? @(:data ov))]]
        (conj! ov 10)
        (conj! ov 20)
        [(mapv #(select-keys % [:index :new-value :old-value]) @output)
         (= (vec (reverse containers)) (mapv :container @output))
         empty-state
         @ov
         (mapv @(:positions ov) @(:data ov))])))
  => [[{:index 2 :new-value nil :old-value 3}
       {:index 1 :new-value nil :old-value false}
       {:index 0 :new-value nil :old-value nil}]
      true [[] {} true] [10 20] [0 1]])

^{:refer ova.core/->Ova :added "1.0"}
(fact "count, seq, and nth operate on element values"
  (let [ov (v/ova-new)]
    [(count ov) (seq ov)
     (nth ov 0 :missing)
     (get ov :unknown :missing)])
  => [0 nil :missing :missing]

  (let [ov (v/ova-new)]
    (doseq [value [nil false 3]]
      (conj! ov value))
    [(count ov)
     (seq ov)
     (mapv identity (seq ov))
     (reduce (fn [n _] (inc n)) 0 ov)
     (first ov)
     (last ov)
     (nth ov 0 :missing)
     (nth ov 1 :missing)
     (nth ov 2)
     (nth ov -1 :missing)
     (nth ov 3 :missing)
     (every? some? [(:data ov) (:watches ov) (:positions ov)])])
  => [3 [nil false 3] [nil false 3] 3 nil 3 nil false 3
      :missing :missing true]

  (nth (v/ova-new) 0)
  => (throws IndexOutOfBoundsException)

  (nth (v/ova-new) -1)
  => (throws IndexOutOfBoundsException)

  (let [ov (v/ova-new)]
    (conj! ov 1)
    (conj! ov 2)
    (let [snapshot (seq ov)]
      (assoc! ov 0 10)
      (pop! ov)
      [snapshot (seq ov) (count ov) (nth ov 0) (persistent! ov)]))
  => [[1 2] [10] 1 10 [10]])

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
