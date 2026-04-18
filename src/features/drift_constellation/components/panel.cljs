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
     [kv-list "Desired" (:desired node)]
     [kv-list "Observed" (:observed node)]]))
