(ns ui.components.controls
  (:require [re-frame.core :as rf]))

(defn accessibility-controls []
  (let [prefs @(rf/subscribe [:prefs])]
    [:section.controls {:aria-label "Preferencias de leitura"}
     [:h2.controls__title "Leitura"]
     [:p.controls__status
      "Tema atual: "
      (if (= :dark (:theme prefs)) "escuro" "claro")
      " | Contraste: "
      (if (:high-contrast? prefs) "alto" "padrao")]
     [:div.controls__row
      [:button.controls__button
       {:type "button"
        :aria-pressed (= :dark (:theme prefs))
        :on-click #(rf/dispatch [:prefs/toggle-theme])}
       (if (= :dark (:theme prefs)) "Usar tema claro" "Usar tema escuro")]
      [:button.controls__button
       {:type "button"
        :aria-pressed (:high-contrast? prefs)
        :on-click #(rf/dispatch [:prefs/toggle-high-contrast])}
       (if (:high-contrast? prefs) "Usar contraste padrao" "Ativar alto contraste")]]
     [:div.controls__row
      [:label {:for "font-scale"} "Tamanho da fonte"]
      [:input {:id "font-scale"
               :type "range"
               :min 0.9
               :max 1.3
               :step 0.05
               :value (:font-scale prefs)
               :on-change #(rf/dispatch [:prefs/set-font-scale (js/parseFloat (.. % -target -value))])}]]
     [:p.controls__status
      "Escala atual: " (.toFixed (:font-scale prefs) 2) "x"]
     [:p.controls__hint
      "Movimento reduzido do sistema: "
      (if (:reduced-motion? prefs) "ativo" "inativo")]]))
