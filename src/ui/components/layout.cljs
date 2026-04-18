(ns ui.components.layout
  (:require [app.router :as router]
            [re-frame.core :as rf]
            [ui.components.controls :as controls]))

(def nav-items
  [{:page :home :label "Home"}
   {:page :about :label "Sobre"}
   {:page :articles :label "Artigos"}
   {:page :experiments :label "Experimentos"}
   {:page :accessibility :label "Acessibilidade"}
   {:page :contact :label "Contato"}])

(defn site-header []
  (let [route @(rf/subscribe [:route])]
    [:header.site-header
     [:div.site-title
      [:p.site-title__name "inside-free-coding notes"]
      [:p.site-title__subtitle "publicacao tecnica autoral"]]
     [:nav {:aria-label "Navegacao principal"}
      [:ul.nav-list
       (for [{:keys [page label]} nav-items]
         ^{:key (name page)}
         [:li
          [:a {:href (router/href page)
               :aria-current (when (= page (:page route)) "page")}
           label]])]]]))

(defn site-footer []
  [:footer.site-footer
   [:p "insidefreecoding.com.br"]
   [:p "prod: stable | lab: experimental"]])

(defn app-shell [main-view]
  (let [{:keys [theme font-scale high-contrast? reduced-motion?]} @(rf/subscribe [:prefs])]
    [:div.site
     {:class [(str "theme-" (name theme))
              (when high-contrast? "high-contrast")
              (when reduced-motion? "reduce-motion")]
      :style {:font-size (str (* 100 font-scale) "%")}}
     [site-header]
     [controls/accessibility-controls]
     [:main#conteudo-principal.site-main {:tab-index -1}
      [main-view]]
     [site-footer]]))
