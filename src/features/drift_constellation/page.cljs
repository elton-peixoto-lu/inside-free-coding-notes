(ns features.drift-constellation.page
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [features.drift-constellation.components.controls :as controls]
            [features.drift-constellation.components.panel :as panel]
            [features.drift-constellation.scene.core :as scene]))

(defn drift-canvas []
  (let [scene-data* (rf/subscribe [:drift/scene-data])
        selected-node* (rf/subscribe [:drift/selected-node])
        paused?* (rf/subscribe [:drift/animation-paused?])
        reset-token* (rf/subscribe [:drift/camera-reset-token])
        scene-ref (atom nil)
        container-ref (atom nil)
        last-reset-token (atom nil)
        last-scene-data (atom nil)]
    (r/create-class
     {:display-name "drift-canvas"
      :component-did-mount
      (fn []
        (when-let [container @container-ref]
          (let [instance (scene/create-scene!
                          container
                          (fn [node-id]
                            (rf/dispatch [:drift/select-node node-id])))]
            (reset! scene-ref instance)
            ((:set-data! instance) @scene-data*)
            (reset! last-scene-data @scene-data*)
            ((:set-selected! instance) (:id @selected-node*))
            ((:set-paused! instance) @paused?*))))
      :component-did-update
      (fn [_ _]
        (when-let [instance @scene-ref]
          (when (not= @last-scene-data @scene-data*)
            ((:set-data! instance) @scene-data*)
            (reset! last-scene-data @scene-data*))
          ((:set-selected! instance) (:id @selected-node*))
          ((:set-paused! instance) @paused?*)
          (when (not= @last-reset-token @reset-token*)
            (reset! last-reset-token @reset-token*)
            ((:reset-camera! instance)))))
      :component-will-unmount
      (fn []
        (when-let [instance @scene-ref]
          ((:dispose! instance))
          (reset! scene-ref nil)))
      :reagent-render
      (fn [_]
        ;; Deref subscriptions here so Reagent re-renders when drift state changes.
        (let [_ @scene-data*
              _ @selected-node*
              _ @paused?*
              _ @reset-token*]
          [:div.drift-canvas
           {:ref #(reset! container-ref %)}]))})))

(defn drift-constellation-page []
  (let [filters* (rf/subscribe [:drift/filters])
        selected-node* (rf/subscribe [:drift/selected-node])
        legend-items* (rf/subscribe [:drift/legend])
        summary* (rf/subscribe [:drift/summary])
        type-options* (rf/subscribe [:drift/type-options])
        paused?* (rf/subscribe [:drift/animation-paused?])
        reset-token* (rf/subscribe [:drift/camera-reset-token])]
    (r/create-class
     {:display-name "drift-constellation-page"
      :component-did-mount #(rf/dispatch [:drift/init])
      :reagent-render
      (fn []
        (let [filters @filters*
              selected-node @selected-node*
              legend-items @legend-items*
              summary @summary*
              type-options @type-options*
              paused? @paused?*
              reset-token @reset-token*]
          [:section.drift-page
           [:header
            [:h1 "Drift Constellation"]
            [:p "O problema nao e so criar infraestrutura. E controlar drift, intencao e reconciliacao."]]
           [:section.drift-summary {:aria-label "Resumo do estado de drift"}
            [:p [:strong "Recursos visiveis: "] (:resource-count summary)]
            [:p [:strong "Criticos: "] (:critical-count summary)]
            [:p [:strong "Hotspots: "] (:hotspot-count summary)]
            [:p [:strong "Risco alto: "] (:high-risk-count summary)]
            [:p [:strong "Drift medio: "] (.toFixed (:avg-drift summary) 2)]]
           [:section.drift-layout
            [drift-canvas {:reset-token reset-token}]
            [panel/drift-side-panel {:node selected-node}]]
           [:section.drift-controls {:aria-label "Controles de visualizacao"}
            [controls/severity-controls (:severity filters)]
            [controls/type-controls (:type filters) type-options]
            [controls/action-controls paused?]
            [controls/legend legend-items]]]))})))
