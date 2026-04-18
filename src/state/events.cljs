(ns state.events
  (:require [re-frame.core :as rf]
            [app.router :as router]
            [cljs.reader :as reader]
            [state.db :as db]))

(defn- normalize-category [item]
  (update item :category #(if (keyword? %) % (keyword %))))

(rf/reg-fx
 :content/load!
 (fn [_]
   (-> (js/fetch "/content/index.edn")
       (.then #(.text %))
       (.then (fn [index-edn]
                (let [files (reader/read-string index-edn)]
                  (-> (js/Promise.all
                       (clj->js
                        (map (fn [file]
                               (-> (js/fetch (str "/content/articles/" file))
                                   (.then #(.text %))
                                   (.then reader/read-string)
                                   (.then normalize-category)))
                             files)))
                      (.then (fn [items]
                               (rf/dispatch [:content/load-success (js->clj items :keywordize-keys true)])))
                      (.catch (fn [err]
                                (rf/dispatch [:content/load-failure (str err)])))))))
       (.catch (fn [err]
                 (rf/dispatch [:content/load-failure (str err)]))))))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   (-> db/default-db
       (assoc :route (router/parse-route (.-hash js/location)))
       (assoc-in [:content :status] :loading))))

(rf/reg-event-fx
 :initialize/load-content
 (fn [_ _]
   {:content/load! true}))

(rf/reg-event-db
 :content/load-success
 (fn [state [_ items]]
   (-> state
       (assoc-in [:content :status] :ready)
       (assoc-in [:content :items] items)
       (assoc-in [:content :error] nil))))

(rf/reg-event-db
 :content/load-failure
 (fn [state [_ error]]
   (-> state
       (assoc-in [:content :status] :error)
       (assoc-in [:content :error] error))))

(rf/reg-event-db
 :route/changed
 (fn [state [_ hash-value]]
   (assoc state :route (router/parse-route hash-value))))

(rf/reg-event-db
 :filters/set-tag
 (fn [state [_ tag]]
   (assoc-in state [:filters :tag] (when-not (= tag "all") tag))))

(rf/reg-event-db
 :filters/set-category
 (fn [state [_ category]]
   (assoc-in state [:filters :category] category)))

(rf/reg-event-db
 :prefs/toggle-theme
 (fn [state _]
   (update-in state [:prefs :theme] #(if (= % :dark) :light :dark))))

(rf/reg-event-db
 :prefs/set-font-scale
 (fn [state [_ value]]
   (assoc-in state [:prefs :font-scale] value)))

(rf/reg-event-db
 :prefs/toggle-high-contrast
 (fn [state _]
   (update-in state [:prefs :high-contrast?] not)))

(rf/reg-event-db
 :prefs/set-reduced-motion
 (fn [state [_ enabled?]]
   (assoc-in state [:prefs :reduced-motion?] enabled?)))
