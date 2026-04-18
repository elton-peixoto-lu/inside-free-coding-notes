(ns ui.components.article-card
  (:require [app.router :as router]))

(defn article-card [{:keys [slug title summary date tags category reading-time]}]
  [:article.card
   [:header
    [:h2.card__title
     [:a {:href (router/href :article slug)} title]]
    [:p.card__meta
     [:span (name category)] " | "
     [:time {:date-time date} date] " | "
     [:span reading-time]]]
   [:p summary]
   [:ul.tag-list {:aria-label "Tags"}
    (for [tag tags]
      ^{:key tag}
      [:li.tag tag])]
   [:p
    [:a {:href (router/href :article slug)} "Ler artigo completo"]]])
