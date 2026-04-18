(ns ui.pages.experiments
  (:require [content.articles :as content]
            [app.router :as router]))

(defn experiments-page []
  (let [items (filter #(= :benchmark (:category %)) content/publications)]
    [:section
     [:header
      [:h1 "Experimentos"]
      [:p "Pagina dedicada a metodologia, dados e reproducao de benchmarks."]]
     [:ul
      (for [{:keys [slug title methodology]} items]
        ^{:key slug}
        [:li
         [:h2 [:a {:href (router/href :article slug)} title]]
         [:p methodology]])]]))
