(ns features.drift-constellation.events
  (:require [re-frame.core :as rf]
            [features.drift-constellation.data :as data]))

(defn- normalize-node [node]
  (assoc node :severity (data/drift->severity (:drift-score node))))

(rf/reg-event-db
 :drift/init
 (fn [db _]
   (if (get-in db [:drift :loaded?])
     db
     (let [{:keys [nodes edges hotspots]} data/mock-graph]
       (-> db
           (assoc-in [:drift :loaded?] true)
           (assoc-in [:drift :nodes] (mapv normalize-node nodes))
           (assoc-in [:drift :edges] edges)
           (assoc-in [:drift :hotspots] hotspots)
           (assoc-in [:drift :selected-node-id] (-> nodes first :id)))))))

(rf/reg-event-db
 :drift/select-node
 (fn [db [_ node-id]]
   (assoc-in db [:drift :selected-node-id] node-id)))

(rf/reg-event-db
 :drift/set-severity-filter
 (fn [db [_ severity]]
   (assoc-in db [:drift :filters :severity] severity)))

(rf/reg-event-db
 :drift/set-type-filter
 (fn [db [_ type-id]]
   (assoc-in db [:drift :filters :type] type-id)))

(rf/reg-event-db
 :drift/toggle-animation
 (fn [db _]
   (update-in db [:drift :animation-paused?] not)))

(rf/reg-event-db
 :drift/reset-camera
 (fn [db _]
   (update-in db [:drift :camera-reset-token] inc)))
