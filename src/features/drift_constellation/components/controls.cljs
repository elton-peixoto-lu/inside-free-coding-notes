(ns features.drift-constellation.components.controls
  (:require [re-frame.core :as rf]))

(defn severity-controls [current]
  [:div.drift-controls__row
   [:label {:for "drift-severity"} "Severidade"]
   [:select {:id "drift-severity"
             :value (name current)
             :on-change #(rf/dispatch [:drift/set-severity-filter (keyword (.. % -target -value))])}
    [:option {:value "all"} "Todas"]
    [:option {:value "healthy"} "Saudavel"]
    [:option {:value "attention"} "Atencao"]
    [:option {:value "high"} "Alto"]
    [:option {:value "critical"} "Critico"]]])

(defn type-controls [current options]
  [:div.drift-controls__row
   [:label {:for "drift-type"} "Tipo de recurso"]
   [:select {:id "drift-type"
             :value (name current)
             :on-change #(rf/dispatch [:drift/set-type-filter (keyword (.. % -target -value))])}
    (for [opt options]
      ^{:key (name opt)}
      [:option {:value (name opt)}
       (if (= opt :all) "Todos" (name opt))])]])

(defn action-controls [paused?]
  [:div.drift-controls__row
   [:button.controls__button
    {:type "button"
     :on-click #(rf/dispatch [:drift/reset-camera])}
    "Reset camera"]
   [:button.controls__button
    {:type "button"
     :aria-pressed paused?
     :on-click #(rf/dispatch [:drift/toggle-animation])}
    (if paused? "Retomar animacao" "Pausar animacao")]])

(defn legend [items]
  [:ul.drift-legend {:aria-label "Legenda de severidade"}
   (for [{:keys [severity label]} items]
     ^{:key (name severity)}
     [:li
      [:span {:class (str "drift-severity-dot drift-severity-dot--" (name severity))
              :aria-hidden "true"}]
      [:span label]])])
