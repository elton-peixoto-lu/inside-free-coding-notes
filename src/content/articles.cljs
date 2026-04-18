(ns content.articles)

(def publications
  [{:slug "benchmark-concatenacao-strings-clojure-dio"
    :title "Benchmark de Concatenacao de Strings em Clojure"
    :summary "Comparacao entre reduce str e StringBuilder para concatenar 100.000 strings, com metodologia reproduzivel e grafico em escala logaritmica."
    :date "2025-06-15"
    :tags ["benchmark" "clojure" "strings" "performance"]
    :category :benchmark
    :reading-time "14 min"
    :type :article
    :toc [{:id "contexto" :label "Contexto"}
          {:id "metodologia" :label "Metodologia"}
          {:id "achados" :label "Codigo e achados"}
          {:id "resultados" :label "Resultados"}
          {:id "conclusao" :label "Conclusao"}
          {:id "reproducao" :label "Como reproduzir"}
          {:id "referencias" :label "Referencias"}]
    :methodology "Entrada: (repeat 100000 \"abc\"). Cada funcao foi executada com criterium.core/quick-benchmark e os resultados foram comparados por media em nanossegundos, incluindo visualizacao em escala logaritmica."
    :diagram-description "Grafico de barras com dois metodos no eixo x (reduce str e StringBuilder) e tempo medio no eixo y em escala log10. A barra de StringBuilder aparece muito abaixo, indicando ganho expressivo."
    :diagram-image "/assets/articles/benchmark-concatenacao-strings-grafico.png"
    :diagram-caption "Comparacao de tempo medio entre reduce str e StringBuilder (escala logaritmica)."
    :code "(ns clojure-benchmarks.core\n  (:require [criterium.core :as c]\n            [incanter.core :as i]\n            [incanter.charts :as charts]))\n\n(def parts (repeat 100000 \"abc\"))\n\n(defn using-str-concat [parts]\n  (reduce str \"\" parts))\n\n(defn using-string-builder [parts]\n  (let [sb (StringBuilder.)]\n    (doseq [part parts]\n      (.append sb part))\n    (.toString sb)))\n\n(defn log10 [x]\n  (Math/log10 (max x 1e-9)))\n\n(defn -main [& _]\n  (println \"Benchmark de concatenacao de strings em Clojure\")\n  (let [res1 (c/quick-benchmark (using-str-concat parts) {})\n        res2 (c/quick-benchmark (using-string-builder parts) {})\n        mean1 (first (:mean res1))\n        mean2 (first (:mean res2))]\n    (when (and (number? mean1) (number? mean2))\n      (let [labels [\"reduce str\" \"StringBuilder\"]\n            vals   [(double mean1) (double mean2)]\n            chart  (charts/bar-chart labels (mapv log10 vals)\n                                     :title \"Concatenacao de Strings (log10 ns)\"\n                                     :y-label \"log10 Tempo medio (ns)\")]\n        (i/view chart)\n        (println (format \"reduce str    : %.2f ns\" (first vals)))\n        (println (format \"StringBuilder : %.2f ns\" (second vals)))))))"
    :body ["Este estudo compara duas abordagens para concatenacao de strings em Clojure: reduce str e StringBuilder."
           "A motivacao foi validar, com dados, o impacto de escolhas simples de implementacao em cenarios com grande volume de dados."
           "A abordagem ingenua cria muitas strings intermediarias, enquanto StringBuilder reutiliza um buffer mutavel."
           "No experimento publicado, o ganho de StringBuilder foi de ordens de grandeza em relacao ao reduce str para 100.000 entradas."]
    :conclusion "Para cargas grandes, prefira StringBuilder (ou clojure.string/join) no lugar de reduce str para concatenacao acumulativa. A escolha melhora desempenho sem alterar o comportamento funcional esperado."
    :reproduce ["Criar projeto Clojure com dependencias criterium e incanter."
                "Definir entrada: (def parts (repeat 100000 \"abc\"))."
                "Executar benchmark das duas funcoes com quick-benchmark."
                "Comparar :mean de cada resultado e gerar grafico em escala log10."
                "Repetir em JVMs e maquinas diferentes para validar consistencia."]
    :references [{:label "Artigo original na DIO"
                  :url "https://web.dio.me/articles/benchmark-de-concatenacao-de-strings-em-clojure-75b017975756"}
                 {:label "Criterium"
                  :url "https://github.com/hugoduncan/criterium"}
                 {:label "Incanter Charts API"
                  :url "https://incanter.github.io/incanter/charts-api.html"}
                 {:label "Java StringBuilder"
                  :url "https://docs.oracle.com/javase/8/docs/api/java/lang/StringBuilder.html"}
                 {:label "Clojure reduce"
                  :url "https://clojuredocs.org/clojure.core/reduce"}]}
   {:slug "benchmark-cljs-data-structures-2026"
    :title "Benchmark: Estruturas de Dados em ClojureScript 2026"
    :summary "Comparacao pratica entre vetores, mapas e transientes em cenarios reais de leitura e escrita."
    :date "2026-03-10"
    :tags ["benchmark" "clojurescript" "performance"]
    :category :benchmark
    :reading-time "18 min"
    :type :article
    :toc [{:id "contexto" :label "Contexto"}
          {:id "metodologia" :label "Metodologia"}
          {:id "resultados" :label "Resultados"}
          {:id "conclusao" :label "Conclusao"}
          {:id "reproducao" :label "Como reproduzir"}
          {:id "referencias" :label "Referencias"}]
    :methodology "Executamos 30 rodadas no browser Chromium, descartando aquecimento inicial e normalizando por operacoes por segundo."
    :diagram-description "Diagrama textual: fluxo inicia em geracao de dataset, passa por leitura/escrita por estrutura e termina em agregacao estatistica."
    :code "(defn run-benchmark [dataset]\n  (-> dataset\n      (update :vector conj 42)\n      (assoc-in [:map :k] :v)))"
    :body ["Comparar estruturas de dados sem contexto quase sempre leva a decisoes ruins."
           "Neste estudo, priorizamos cenarios de manutencao de estado em interfaces ricas."
           "Vetores se destacaram em leitura sequencial; mapas em atualizacao indexada."
           "Transientes ganharam em lotes grandes, com custo de legibilidade."]
    :conclusion "Escolha a estrutura pela forma de acesso dominante e nao por microbenchmark isolado."
    :reproduce ["npm install"
                "npx shadow-cljs watch app"
                "Abrir /experimentos e executar o script de coleta"
                "Comparar CSV final no diretorio artifacts"]
    :references [{:label "ClojureScript Reference"
                  :url "https://clojurescript.org"}
                 {:label "re-frame Docs"
                  :url "https://day8.github.io/re-frame/"}]}
   {:slug "paper-short-semantic-html-reading"
    :title "Paper Curto: HTML Semantico para Leitura Tecnica"
    :summary "Hipoteses e evidencias sobre como semantica melhora navegacao assistiva em artigos longos."
    :date "2026-02-01"
    :tags ["a11y" "semantica" "html"]
    :category :paper
    :reading-time "12 min"
    :type :paper
    :toc [{:id "contexto" :label "Contexto"}
          {:id "metodologia" :label "Metodologia"}
          {:id "achados" :label "Achados"}
          {:id "conclusao" :label "Conclusao"}
          {:id "reproducao" :label "Como reproduzir"}
          {:id "referencias" :label "Referencias"}]
    :methodology "Revisao de 12 paginas tecnicas com e sem landmarks semanticos, medindo tempo de orientacao via leitor de tela."
    :diagram-description "Diagrama textual: dois grupos de paginas (semanticas e genericas) alimentam metricas de tempo e erro."
    :code "<main>\n  <article>\n    <header>...</header>\n    <section aria-labelledby=\"metodologia\">...</section>\n  </article>\n</main>"
    :body ["A principal diferenca nao apareceu em velocidade de leitura, mas em orientacao."
           "Landmarks e hierarquia de headings reduziram erros de navegacao em tarefas de busca de secao."
           "Skip link e foco visivel reduziram abandonos em navegacao por teclado."]
    :conclusion "Semantica nao e detalhe de SEO; e infraestrutura de compreensao e autonomia."
    :reproduce ["Criar duas versoes da mesma pagina"
                "Executar tarefas guiadas com NVDA ou VoiceOver"
                "Registrar tempo para encontrar metodologia e conclusao"]
    :references [{:label "W3C WAI Page Structure"
                  :url "https://www.w3.org/WAI/tutorials/page-structure/"}
                 {:label "MDN Semantic HTML"
                  :url "https://developer.mozilla.org/en-US/docs/Glossary/Semantics"}]}
   {:slug "analise-custo-renderizacao-reagent"
    :title "Analise: Custo de Renderizacao em Reagent"
    :summary "Onde o custo real aparece em telas de leitura e como reduzir rerender sem complexidade desnecessaria."
    :date "2026-01-15"
    :tags ["reagent" "render" "analise"]
    :category :analysis
    :reading-time "15 min"
    :type :analysis
    :toc [{:id "contexto" :label "Contexto"}
          {:id "metodologia" :label "Metodologia"}
          {:id "achados" :label "Achados"}
          {:id "conclusao" :label "Conclusao"}
          {:id "reproducao" :label "Como reproduzir"}
          {:id "referencias" :label "Referencias"}]
    :methodology "Instrumentamos componentes com marcadores de render e observamos interacoes de filtro em 5k itens sinteticos."
    :diagram-description "Diagrama textual: evento de filtro atualiza db, subs recalculam selecao, componentes de lista rerenderizam."
    :code "(defn article-list []\n  (let [items @(rf/subscribe [:filtered-publications])]\n    [:ul (for [item items] ^{:key (:slug item)} [row item])]))"
    :body ["O custo dominante veio da renderizacao de lista completa em mudancas pequenas de filtro."
           "Ajustes simples em granularidade de subscription trouxeram ganho significativo."
           "Nao usamos memoizacao agressiva; mantivemos clareza e previsibilidade."]
    :conclusion "Comece por estrutura de dados e subscriptions antes de otimizar componentes."
    :reproduce ["Gerar dataset sintetico com 5k registros"
                "Ativar contador de render por item"
                "Aplicar filtros por tag e categoria e comparar metricas"]
    :references [{:label "Reagent FAQ"
                  :url "https://github.com/reagent-project/reagent/blob/master/doc/FAQ/README.md"}
                 {:label "re-frame Flow Mechanics"
                  :url "https://day8.github.io/re-frame/flow-mechanics/"}]}])

(defn by-slug [slug]
  (some #(when (= slug (:slug %)) %) publications))

(def categories
  [{:id :all :label "Todas"}
   {:id :benchmark :label "Benchmark"}
   {:id :analysis :label "Analise"}
   {:id :paper :label "Paper"}])

(def all-tags
  (->> publications
       (mapcat :tags)
       distinct
       sort))
