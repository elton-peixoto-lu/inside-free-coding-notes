(ns ui.pages.experiments
  (:require [content.articles :as content]
            [app.router :as router]))

(defn experiments-page []
  (let [items (filter #(= :benchmark (:category %)) content/publications)]
    [:section
     [:header
      [:h1 "Experimentos"]
      [:p "Pagina dedicada a metodologia, dados e reproducao de benchmarks."]]
     [:article.card
      [:h2
       [:a {:href (router/href :drift-constellation)}
        "Drift Constellation"]]
      [:p "Visualizacao 3D para analisar divergencia entre estado desejado e observado na plataforma."]
      [:p
       [:a {:href (router/href :drift-constellation)}
        "Abrir experimento"]]]
     [:ul
      (for [{:keys [slug title methodology]} items]
        ^{:key slug}
        [:li
         [:h2 [:a {:href (router/href :article slug)} title]]
         [:p methodology]])]]))
