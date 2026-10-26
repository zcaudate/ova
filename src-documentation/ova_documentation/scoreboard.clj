(ns ova-documentation.scoreboard
  (:require [ova.core :as ova]
            [ova.select :as select])
  (:use code.test))

[[:chapter {:title "Track player scores"}]]

"This example uses predicates to find players and update events to observe high scores. A watch compares the old and new high-score fields explicitly. The updates go through Ova so notifications are emitted."

(fact "record attempts and announce new high scores"
  (let [scoreboard (ova/ova-new)
        notifications (atom [])]
    (doseq [player [{:name "Bill" :attempts 0 :highest 0}
                    {:name "Sally" :attempts 0 :highest 0}]]
      (ova/ova-item-push scoreboard player))
    (ova/ova-watch-add
     scoreboard :high-score :ova/update
     (fn [_ {:keys [old-value new-value]}]
       (when (not= (:highest old-value) (:highest new-value))
         (swap! notifications conj [(:name new-value) (:highest new-value)]))))
    (doseq [score [35 20 40]]
      (select/ova-update-where
       scoreboard #(= "Bill" (:name %))
       (fn [player]
         (-> player
             (update :attempts inc)
             (update :highest max score)))))
    [@scoreboard @notifications])
  => [[{:name "Bill" :attempts 3 :highest 40}
       {:name "Sally" :attempts 0 :highest 0}]
      [["Bill" 35] ["Bill" 40]]])

"The watch ignores attempts that do not change the highest score. This replaces the old example's ref mutations, selector syntax, and field-change watch helpers with the current API."
