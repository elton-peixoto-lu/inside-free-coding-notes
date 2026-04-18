(ns ui.pages.accessibility)

(defn accessibility-page []
  [:article
   [:header
    [:h1 "Acessibilidade"]
    [:p "Compromisso com leitura tecnica acessivel por teclado, leitor de tela e ajustes de conforto visual."]]
   [:section
    [:h2 "Recursos ativos"]
    [:ul
     [:li "Skip link para ir direto ao conteudo principal."]
     [:li "Navegacao por teclado com foco visivel."]
     [:li "Tema claro/escuro, alto contraste e escala de fonte."]
     [:li "Respeito a prefers-reduced-motion do sistema."]
     [:li "Descricao textual para diagramas em publicacoes."]]]
   [:section
    [:h2 "Observacao"]
    [:p "Usamos HTML semantico como base e evitamos ARIA desnecessario."]]])
