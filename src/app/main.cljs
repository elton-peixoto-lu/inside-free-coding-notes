(ns app.main
  (:require [a11y.preferences :as a11y]
            [features.drift-constellation.events]
            [features.drift-constellation.page :as drift-constellation]
            [features.drift-constellation.subs]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [state.events]
            [state.subs]
            [ui.components.layout :as layout]
            [ui.pages.about :as about]
            [ui.pages.accessibility :as accessibility]
            [ui.pages.article :as article]
            [ui.pages.articles :as articles]
            [ui.pages.contact :as contact]
            [ui.pages.experiments :as experiments]
            [ui.pages.home :as home]))

(defn current-page []
  (let [{:keys [page slug]} @(rf/subscribe [:route])]
    (case page
      :about [about/about-page]
      :articles [articles/articles-page]
      :article [article/article-page slug]
      :experiments [experiments/experiments-page]
      :drift-constellation [drift-constellation/drift-constellation-page]
      :accessibility [accessibility/accessibility-page]
      :contact [contact/contact-page]
      [home/home-page])))

(defn root []
  [layout/app-shell current-page])

(defn mount []
  (rdom/render [root] (.getElementById js/document "app")))

(defn on-route-change []
  (rf/dispatch [:route/changed (.-hash js/location)])
  (.focus (.getElementById js/document "conteudo-principal")))

(defn init []
  (rf/dispatch-sync [:initialize])
  (rf/dispatch [:initialize/load-content])
  (a11y/sync-system-motion!)
  (.addEventListener js/window "hashchange" on-route-change)
  (mount))
