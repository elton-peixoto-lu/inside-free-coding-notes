(ns ui.pages.articles
  (:require [re-frame.core :as rf]
            [ui.components.article-card :as card]))

(defn articles-page []
  (let [{:keys [tag category]} @(rf/subscribe [:filters])
        items @(rf/subscribe [:filtered-publications])
        categories @(rf/subscribe [:categories])
        tags @(rf/subscribe [:all-tags])
        content-status @(rf/subscribe [:content-status])
        content-error @(rf/subscribe [:content-error])]
    [:section
     [:header
      [:h1 "Artigos"]
      [:p "Filtre por categoria e tag para encontrar leituras especificas."]]
     (when (= :loading content-status)
       [:p "Carregando publicacoes em EDN..."])
     (when (= :error content-status)
       [:p "Falha ao carregar publicacoes: " content-error])
     [:section.filters {:aria-label "Filtros"}
      [:div
       [:label {:for "category-filter"} "Categoria"]
       [:select {:id "category-filter"
                 :value (name category)
                 :on-change #(rf/dispatch [:filters/set-category (keyword (.. % -target -value))])}
        (for [{:keys [id label]} categories]
          ^{:key (name id)}
          [:option {:value (name id)} label])]]
      [:div
       [:label {:for "tag-filter"} "Tag"]
       [:select {:id "tag-filter"
                 :value (or tag "all")
                 :on-change #(rf/dispatch [:filters/set-tag (.. % -target -value)])}
        [:option {:value "all"} "Todas"]
        (for [t tags]
          ^{:key t}
          [:option {:value t} t])]]]
     [:section {:aria-live "polite"}
      [:h2 "Resultados"]
      (if (seq items)
        (for [item items]
          ^{:key (:slug item)}
          [card/article-card item])
        [:p "Nenhum artigo para os filtros atuais."])]]))
