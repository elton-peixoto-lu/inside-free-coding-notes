(ns a11y.preferences
  (:require [re-frame.core :as rf]))

(defn sync-system-motion! []
  (let [query (.matchMedia js/window "(prefers-reduced-motion: reduce)")]
    (rf/dispatch [:prefs/set-reduced-motion (.-matches query)])
    (.addEventListener query
                       "change"
                       (fn [event]
                         (rf/dispatch [:prefs/set-reduced-motion (.-matches event)])))))
