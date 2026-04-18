(ns state.db)

(def default-db
  {:route {:page :home}
   :content {:status :idle
             :items []
             :error nil}
   :filters {:tag nil
             :category :all}
   :prefs {:theme :light
           :font-scale 1
           :high-contrast? false
           :reduced-motion? false}})
