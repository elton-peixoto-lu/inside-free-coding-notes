(ns features.drift-constellation.subs
  (:require [re-frame.core :as rf]
            [features.drift-constellation.data :as data]))

(rf/reg-sub :drift/raw (fn [db _] (:drift db)))
(rf/reg-sub :drift/filters :<- [:drift/raw] (fn [drift _] (:filters drift)))
(rf/reg-sub :drift/animation-paused? :<- [:drift/raw] (fn [drift _] (:animation-paused? drift)))
(rf/reg-sub :drift/camera-reset-token :<- [:drift/raw] (fn [drift _] (:camera-reset-token drift)))

(rf/reg-sub
 :drift/nodes
 :<- [:drift/raw]
 :<- [:drift/filters]
 (fn [[drift {:keys [severity type]}] _]
   (->> (:nodes drift)
        (filter (fn [node]
                  (and (or (= :all severity) (= severity (:severity node)))
                       (or (= :all type) (= type (:type node))))))
        vec)))

(rf/reg-sub
 :drift/node-by-id
 :<- [:drift/raw]
 (fn [drift [_ node-id]]
   (some #(when (= node-id (:id %)) %) (:nodes drift))))

(rf/reg-sub
 :drift/selected-node
 :<- [:drift/raw]
 (fn [drift _]
   (let [node-id (:selected-node-id drift)]
     (some #(when (= node-id (:id %)) %) (:nodes drift)))))

(rf/reg-sub
 :drift/type-options
 :<- [:drift/raw]
 (fn [drift _]
   (into [:all] (data/distinct-types (:nodes drift)))))

(rf/reg-sub
 :drift/edges
 :<- [:drift/raw]
 :<- [:drift/nodes]
 (fn [[drift nodes] _]
   (let [node-ids (set (map :id nodes))]
     (->> (:edges drift)
          (filter (fn [{:keys [source target]}]
                    (and (contains? node-ids source)
                         (contains? node-ids target))))
          vec))))

(rf/reg-sub
 :drift/hotspots
 :<- [:drift/raw]
 :<- [:drift/nodes]
 (fn [[drift nodes] _]
   (let [node-ids (set (map :id nodes))]
     (->> (:hotspots drift)
          (filter (fn [{:keys [resource-ids]}]
                    (some node-ids resource-ids)))
          vec))))

(rf/reg-sub
 :drift/scene-data
 :<- [:drift/nodes]
 :<- [:drift/edges]
 :<- [:drift/hotspots]
 (fn [[nodes edges hotspots] _]
   {:nodes nodes :edges edges :hotspots hotspots}))

(rf/reg-sub
 :drift/legend
 (fn [_ _]
   [{:severity :healthy :label "Saudavel (0.0 - 0.2)"}
    {:severity :attention :label "Atencao (0.2 - 0.5)"}
    {:severity :high :label "Alto (0.5 - 0.8)"}
    {:severity :critical :label "Critico (> 0.8)"}]))

(rf/reg-sub
 :drift/summary
 :<- [:drift/nodes]
 :<- [:drift/hotspots]
 (fn [[nodes hotspots] _]
   (let [count-all (count nodes)
         critical (count (filter #(= :critical (:severity %)) nodes))
         avg-drift (if (pos? count-all)
                     (/ (reduce + (map :drift-score nodes)) count-all)
                     0.0)
         high-risk (count (filter #(>= (:risk-score %) 0.7) nodes))]
     {:resource-count count-all
      :critical-count critical
      :high-risk-count high-risk
      :hotspot-count (count hotspots)
      :avg-drift avg-drift})))
