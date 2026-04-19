(ns features.drift-constellation.page
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [features.drift-constellation.components.controls :as controls]
            [features.drift-constellation.components.panel :as panel]
            [features.drift-constellation.scene.core :as scene]))

(defn live-reading-copy [selected-node summary]
  (cond
    selected-node
    (str "Recurso em foco: " (:label selected-node)
         ". Severidade " (name (:severity selected-node))
         " com drift " (.toFixed (:drift-score selected-node) 2) ".")

    (pos? (:critical-count summary))
    (str "Ha " (:critical-count summary) " recursos criticos visiveis. Comece pelos halos e pelas linhas mais vibrantes.")

    (pos? (:resource-count summary))
    "A topologia esta relativamente estavel. Use filtros para isolar um tipo de recurso."

    :else
    "Nenhum recurso corresponde aos filtros atuais."))

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
        doc* (rf/subscribe [:publication-by-slug "drift-constellation-documentacao"])
        summary* (rf/subscribe [:drift/summary])
        type-options* (rf/subscribe [:drift/type-options])
        paused?* (rf/subscribe [:drift/animation-paused?])
        running?* (rf/subscribe [:drift/runtime-running?])
        tick* (rf/subscribe [:drift/tick])
        timeline* (rf/subscribe [:drift/timeline])
        reset-token* (rf/subscribe [:drift/camera-reset-token])]
    (r/create-class
     {:display-name "drift-constellation-page"
      :component-did-mount (fn []
                             (rf/dispatch [:drift/init])
                             (rf/dispatch [:drift/start-runtime]))
      :component-will-unmount #(rf/dispatch [:drift/stop-runtime])
      :reagent-render
      (fn []
        (let [filters @filters*
              selected-node @selected-node*
              legend-items @legend-items*
              doc @doc*
              summary @summary*
              type-options @type-options*
              paused? @paused?*
              running? @running?*
              tick @tick*
              timeline @timeline*
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
            [:p [:strong "Drift medio: "] (.toFixed (:avg-drift summary) 2)]
            [:p [:strong "Convergencia: "] (.toFixed (:reconcile-progress summary) 1) "%"]]
           [:section.drift-layout
            [:div.drift-canvas-wrap
             [drift-canvas {:reset-token reset-token}]
             [:aside.drift-canvas-overlay {:aria-label "Leitura rapida da cena"}
              [:p.drift-canvas-overlay__eyebrow "Leitura rapida"]
              [:p (live-reading-copy selected-node summary)]
              [:ul
               [:li "Ponto = recurso"]
               [:li "Halo = hotspot"]
               [:li "Linha vibrante = dependencia sob tensao"]]]]
            [panel/drift-side-panel {:node selected-node}]]
           [:section.drift-controls {:aria-label "Controles de visualizacao"}
            [controls/severity-controls (:severity filters)]
            [controls/type-controls (:type filters) type-options]
           [controls/action-controls paused? running? tick]
           [controls/legend legend-items]]
           [:section.drift-guide {:aria-label "Guia de interpretacao do grafico"}
            [:h2 "Como interpretar o grafico"]
            (if doc
              [:<>
               (for [paragraph (:body doc)]
                 ^{:key paragraph}
                 [:p paragraph])
               [:ul
                (for [item (:interpretation doc)]
                  ^{:key item}
                  [:li item])]
               [:details
                [:summary "Referencias oficiais"]
                [:ul
                 (for [{:keys [label url]} (:references doc)]
                   ^{:key url}
                   [:li [:a {:href url :target "_blank" :rel "noreferrer"} label]])]]]
              [:p "Carregando documentacao EDN do Drift..."])]
           [:section.drift-timeline {:aria-label "Reconcile Timeline"}
            [:h2 "Reconcile Timeline"]
            (if (seq timeline)
              [:ol
               (for [{:keys [id tick label changed-keys drift-score]} timeline]
                 ^{:key id}
                 [:li
                  [:strong (str "T" tick " - " label)]
                  [:span " | campos: "
                   (if (= changed-keys ["scan"]) "scan" (str/join ", " changed-keys))]
                  [:span " | drift: " (.toFixed (or drift-score 0) 2)]])]
              [:p "Aguardando eventos de reconcile..."])]]))})))
