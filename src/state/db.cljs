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
           :reduced-motion? false}
   :drift {:loaded? false
           :nodes []
           :edges []
           :hotspots []
           :selected-node-id nil
           :filters {:severity :all
                     :type :all}
           :animation-paused? false
           :camera-reset-token 0
           :runtime {:running? false
                     :tick 0}
           :timeline {:entries []
                      :window-size 36}}})
