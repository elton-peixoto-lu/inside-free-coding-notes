(ns features.drift-constellation.events
  (:require ["./sim/reconcile_engine.js" :as reconcile-engine]
            [re-frame.core :as rf]
            [features.drift-constellation.data :as data]))

(defn- normalize-node [node]
  (assoc node :severity (data/drift->severity (:drift-score node))))

(defn- normalize-runtime-node [node]
  (let [drift (or (:drift-score node) (:drift_score node) (:driftScore node) 0)]
    (-> node
        (assoc :drift-score drift)
        (assoc :severity (data/drift->severity drift))
        (dissoc :drift_score :driftScore))))

(defonce runtime-timer* (atom nil))

(rf/reg-fx
 :drift/start-runtime-loop
 (fn [_]
   (when-not @runtime-timer*
     (reset! runtime-timer*
             (js/setInterval
              #(rf/dispatch [:drift/tick])
              1200)))))

(rf/reg-fx
 :drift/stop-runtime-loop
 (fn [_]
   (when @runtime-timer*
     (js/clearInterval @runtime-timer*)
     (reset! runtime-timer* nil))))

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
           (assoc-in [:drift :selected-node-id] (-> nodes first :id))
           (assoc-in [:drift :runtime :tick] 0)
           (assoc-in [:drift :timeline :entries] []))))))

(rf/reg-event-fx
 :drift/start-runtime
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:drift :runtime :running?] true)
    :drift/start-runtime-loop true}))

(rf/reg-event-fx
 :drift/stop-runtime
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:drift :runtime :running?] false)
    :drift/stop-runtime-loop true}))

(rf/reg-event-fx
 :drift/toggle-runtime
 (fn [{:keys [db]} _]
   (let [running? (get-in db [:drift :runtime :running?])]
     (if running?
       {:dispatch [:drift/stop-runtime]}
       {:dispatch [:drift/start-runtime]}))))

(rf/reg-event-db
 :drift/tick
 (fn [db _]
   (if-not (get-in db [:drift :runtime :running?])
     db
     (let [tick (inc (get-in db [:drift :runtime :tick]))
           nodes (get-in db [:drift :nodes])
           edges (get-in db [:drift :edges])
           js-result (reconcile-engine/reconcileStep (clj->js nodes) (clj->js edges) tick)
           next-nodes (->> (js->clj (.-nodes js-result) :keywordize-keys true)
                           (mapv normalize-runtime-node))
           new-entries (js->clj (.-timelineEntries js-result) :keywordize-keys true)
           max-window (get-in db [:drift :timeline :window-size])
           timeline (->> (concat new-entries (get-in db [:drift :timeline :entries]))
                         (take max-window)
                         vec)]
       (-> db
           (assoc-in [:drift :runtime :tick] tick)
           (assoc-in [:drift :nodes] next-nodes)
           (assoc-in [:drift :timeline :entries] timeline))))))

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
