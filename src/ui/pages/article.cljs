(ns ui.pages.article
  (:require [re-frame.core :as rf]))

(defn section-list [title items]
  [:section
   [:h2 title]
   [:ol
    (for [item items]
      ^{:key item}
      [:li item])]])

(defn article-page [slug]
  (let [article @(rf/subscribe [:publication-by-slug slug])]
    (if-not article
      [:article
       [:h1 "Artigo nao encontrado"]
       [:p "O slug informado nao existe no repositorio de publicacoes."]]
      (let [{:keys [title summary date tags reading-time toc methodology diagram-description diagram-image diagram-caption code body conclusion reproduce references]} article]
        [:article
         [:header
          [:h1 title]
          [:p summary]
          [:p
           [:time {:date-time date} date] " | "
           [:span reading-time]]
          [:ul.tag-list {:aria-label "Tags do artigo"}
           (for [tag tags]
             ^{:key tag}
             [:li.tag tag])]]
         [:nav {:aria-label "Indice do artigo"}
          [:h2 "Indice"]
          [:ol
           (for [{:keys [id label]} toc]
             ^{:key (name id)}
             [:li [:a {:href (str "#" (name id))} label]])]]
         [:section {:id "contexto"}
          [:h2 "Contexto"]
          (for [paragraph body]
            ^{:key paragraph}
            [:p paragraph])]
         [:section {:id "metodologia"}
          [:h2 "Metodologia"]
          [:p methodology]]
         [:section {:id "achados"}
          [:h2 "Achados e Codigo"]
          [:pre
           [:code code]]]
         [:section {:id "resultados"}
          [:h2 "Resultados"]
          [:p "Os resultados completos estao documentados nos experimentos associados a esta publicacao."]
          (when diagram-image
            [:figure
             [:img {:src diagram-image
                    :alt (or diagram-caption "Grafico do experimento")}]
             (when diagram-caption
               [:figcaption diagram-caption])])]
         [:section {:id "conclusao"}
          [:h2 "Conclusao"]
          [:p conclusion]]
         [:section {:id "reproducao"}
          [:h2 "Como reproduzir"]
          [:ol
           (for [step reproduce]
             ^{:key step}
             [:li step])]]
         [:section {:aria-label "Descricao textual do diagrama"}
          [:h2 "Diagrama em texto"]
          [:p diagram-description]]
         [:section {:id "referencias"}
          [:h2 "Referencias"]
          [:ul
           (for [{:keys [label url]} references]
             ^{:key url}
             [:li [:a {:href url} label]])]]]))))
