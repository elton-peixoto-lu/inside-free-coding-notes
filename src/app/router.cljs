(ns app.router
  (:require [clojure.string :as str]))

(defn- split-path [hash-value]
  (-> hash-value
      (str/replace #"^#/" "")
      (str/split #"/")
      (->> (remove str/blank?))))

(defn parse-route [hash-value]
  (let [[segment slug extra] (split-path hash-value)]
    (case segment
      "sobre" {:page :about}
      "artigos" (if slug
                  {:page :article :slug slug}
                  {:page :articles})
      "experimentos" (if (= slug "drift-constellation")
                       {:page :drift-constellation :lab extra}
                       {:page :experiments})
      "acessibilidade" {:page :accessibility}
      "contato" {:page :contact}
      {:page :home})))

(defn href [page & [slug]]
  (case page
    :home "#/"
    :about "#/sobre"
    :articles "#/artigos"
    :article (str "#/artigos/" slug)
    :experiments "#/experimentos"
    :drift-constellation "#/experimentos/drift-constellation"
    :accessibility "#/acessibilidade"
    :contact "#/contato"
    "#/"))
