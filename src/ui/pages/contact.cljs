(ns ui.pages.contact)

(defn contact-page []
  [:section
   [:header
    [:h1 "Contato"]
    [:p "Sugestoes de tema, correcoes e colaboracoes sao bem-vindas."]]
   [:address
    [:p "Email: " [:a {:href "mailto:hello@insidefreecoding.com.br"} "hello@insidefreecoding.com.br"]]
    [:p "Dominio principal: " [:a {:href "https://insidefreecoding.com.br"} "insidefreecoding.com.br"]]
    [:p "Laboratorio: " [:a {:href "https://labs.insidefreecoding.com.br"} "labs.insidefreecoding.com.br"]]]])
