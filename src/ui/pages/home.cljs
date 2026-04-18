(ns ui.pages.home
  (:require [re-frame.core :as rf]
            [ui.components.article-card :as card]))

(defn home-page []
  (let [items (take 3 @(rf/subscribe [:filtered-publications]))]
    [:section
     [:header
      [:h1 "Publicacao tecnica para leitura profunda"]
      [:p "Textos longos, benchmarks, analises e papers curtos sobre engenharia de software."]]
     [:section {:aria-labelledby "destaques"}
      [:h2#destaques "Publicacoes recentes"]
      (for [item items]
        ^{:key (:slug item)}
        [card/article-card item])]]))
