(ns ova.core
  (:require [clojure.string :as str]
            [clojure.set :as set]))

;;
;; Ova is split into two different watches
;;
;;   1. watches that fire when the whole array changes
;;   2. watches that fire for each element when triggered
;;   3. watches have the following events
;;
;;      - item update    [:ova :container :index :new-value :old-value]
;;      - item added      [:ova :container :index :new-value]
;;      - item removed    [:ova :container :index :old-value]
;;      
;;   So basically, each atom when changed triggers the top level item changed
;;   the issue is that the index of the item can change so something needs to track the index
;;

(defprotocol IOvaWatch
  (-add-watch    [ova key type handler])
  (-remove-watch [ova key]))

(defprotocol IOvaMutate
  (-push-item    [ova item])
  (-pop-item     [ova]))


;;
;; Ova Event
;;

(defrecord OvaEvent [type container index new-value old-value])

(defmethod print-method
  OvaEvent
  [e ^java.io.Writer w]
  (.write w (str "#ova." (name (:type e)))))


;;
;; Ova watch
;;


(defn ova-watch-remove
  "removes a watch"
  {:added "1.0"}
  [ova key]
  (let [{:keys [watches]} ova]
    (swap! watches dissoc key)))

(defn ova-watch-add
  "adds a watch"
  {:added "1.0"}
  [ova key type handler]
  (let [{:keys [watches]} ova]
    (swap! watches assoc key {:type type :handler handler})))

(defn ova-watch-trigger
  "triggers a watch"
  {:added "1.0"}
  [{:keys [watches] :as ova} {:keys [type] :as event}]
  (let [active (filter (fn [[key watch]]
                         (= type (:type watch)))
                       @watches)]
    
    (doseq [[key {:keys [handler]}] active]
      (handler key event))))

(defn ova-index-make
  "makes an array index"
  {:added "1.0"}
  [data]
  (reduce (fn [m [i e]]
            (assoc m e i))
          {}
          (map-indexed vector data)))

(defn ova-item-push
  "pushes an element into the ova"
  {:added "1.0"}
  [{:keys [data positions] :as ova} item]
  (let [container  (atom item)
        ndata      (swap! data  conj  container)
        nindex     (dec (count ndata))
        npositions (swap! positions assoc container nindex)
        _ (ova-watch-trigger ova (OvaEvent. :ova/push
                                            container
                                            nindex
                                            item
                                            nil))]
    ova))

(defn ova-item-pop
  "pops an element from the ova"
  {:added "1.0"}
  [{:keys [data positions] :as ova}]
  (let [[before _] (swap-vals! data #(if (empty? %) [] (pop %)))]
    (when (seq before)
      (let [container (peek before)]
        (swap! positions dissoc container)
        (ova-watch-trigger ova
                           (OvaEvent. :ova/pop
                                      container
                                      (dec (count before))
                                      nil
                                      @container))))
    ova))

(defn ova-item-update
  "updates an element in the ova"
  {:added "1.0"}
  [{:keys [data positions] :as ova} index f]
  (let [vold-value  (volatile! nil) 
        container   (get @data index)]
    (cond (nil? container)
          ova

          :else
          (let [new-value (swap! container (fn [old]
                                             (vreset! vold-value old)
                                             (f old)))]
            (ova-watch-trigger ova
                               (OvaEvent. :ova/update
                                          container
                                          index
                                          new-value
                                          @vold-value))))
    ova))

(deftype Ova [data watches positions]

  clojure.lang.ILookup
  (valAt [ova key]
    (.valAt ova key nil))
  (valAt [_ key not-found]
    (case key
      :data data
      :watches watches
      :positions positions
      not-found))

  clojure.lang.Counted
  (count [_] (count @data))

  clojure.lang.Seqable
  (seq [_] (seq (mapv deref @data)))

  clojure.lang.Indexed
  (nth [_ index]
    @(nth @data index))
  (nth [_ index not-found]
    (let [items @data]
      (if (< -1 index (count items))
        @(nth items index)
        not-found)))
  
  clojure.lang.IDeref
  (deref [ova] (mapv deref @data))

  clojure.lang.ITransientCollection
  (conj [ova item]
    (ova-item-push ova item))

  (persistent [ova]
    (mapv deref @data))

  clojure.lang.ITransientAssociative
  (assoc [ova index item]
    (ova-item-update ova index (fn [_] item)))

  clojure.lang.ITransientVector
  (assocN [ova index item]
    (ova-item-update ova index (fn [_] item)))

  (pop [ova]
    (ova-item-pop ova)))

(extend-type Ova
  IOvaWatch
  (-add-watch    [ova key type handler]
    (ova-watch-add ova key type handler))
  (-remove-watch [ova key]
    (ova-watch-remove ova key))

  IOvaMutate
  (-push-item    [ova item]
    (ova-item-push ova item))
  (-pop-item     [ova]
    (ova-item-pop ova)))

(defn ova-new
  "creates a new ova"
  {:added "1.0"}
  []
  (Ova. (atom [])
        (atom {})
        (atom {})))

(defmethod print-method
  Ova
  [{:keys [data]} ^java.io.Writer w]
  (.write w (str "#ova " {:items (count @data)})))


(comment
  (code.manage/scaffold)
  
  )
