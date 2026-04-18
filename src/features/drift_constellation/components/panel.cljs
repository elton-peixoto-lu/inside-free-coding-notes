(ns features.drift-constellation.components.panel
  (:require [clojure.string :as str]))

(defn format-score [value]
  (.toFixed value 2))

(defn kv-list [title data]
  [:section.drift-panel__block
   [:h3 title]
   [:dl.drift-panel__kv
    (for [[k v] data]
      ^{:key (name k)}
      [:<>
       [:dt (-> k name (str/replace "-" " "))]
       [:dd (str v)]])]])

(defn drift-diffs [desired observed]
  (let [all-keys (->> (concat (keys desired) (keys observed)) distinct sort)]
    (->> all-keys
         (keep (fn [k]
                 (let [d (get desired k)
                       o (get observed k)]
                   (when (not= d o)
                     {:key k :desired d :observed o})))))))

(defn drift-side-panel [{:keys [node]}]
  (if-not node
    [:aside.drift-panel
     [:h2 "Detalhes do recurso"]
     [:p "Selecione um no no grafo para inspecionar estado desejado e observado."]]
    [:aside.drift-panel
     [:h2 "Detalhes do recurso"]
     [:p.drift-panel__title (:label node)]
     [:p.drift-panel__meta
      (str/upper-case (name (:type node))) " | " (str/upper-case (name (:severity node)))]
     [:dl.drift-panel__scores
      [:dt "Drift score"] [:dd (format-score (:drift-score node))]
      [:dt "Risk score"] [:dd (format-score (:risk-score node))]
      [:dt "Cost score"] [:dd (format-score (:cost-score node))]]
     [:section.drift-panel__block
      [:h3 "Delta (Desired x Observed)"]
      (let [diffs (drift-diffs (:desired node) (:observed node))]
        (if (seq diffs)
          [:ul.drift-panel__diffs
           (for [{:keys [key desired observed]} diffs]
             ^{:key (name key)}
             [:li
              [:strong (-> key name (str/replace "-" " "))]
              [:span " desired=" (str desired)]
              [:span " observed=" (str observed)]])]
          [:p "Sem divergencia para este recurso."]))]
     [kv-list "Desired" (:desired node)]
     [kv-list "Observed" (:observed node)]]))
