(ns ui.pages.about)

(defn about-page []
  [:article
   [:header
    [:h1 "Sobre"]
    [:p "inside-free-coding notes e um espaco editorial para publicar experimentos e aprendizados tecnicos de forma clara e reproduzivel."]]
   [:section
    [:h2 "Principios"]
    [:ul
     [:li "Conteudo primeiro, interface discreta."]
     [:li "Semantica forte para leitura assistiva."]
     [:li "Metodologia e reproducao acima de opiniao isolada."]]]])
