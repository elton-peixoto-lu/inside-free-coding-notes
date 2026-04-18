(ns state.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub :route (fn [db _] (:route db)))
(rf/reg-sub :filters (fn [db _] (:filters db)))
(rf/reg-sub :prefs (fn [db _] (:prefs db)))
(rf/reg-sub :theme (fn [db _] (get-in db [:prefs :theme])))
(rf/reg-sub :font-scale (fn [db _] (get-in db [:prefs :font-scale])))
(rf/reg-sub :high-contrast? (fn [db _] (get-in db [:prefs :high-contrast?])))
(rf/reg-sub :reduced-motion? (fn [db _] (get-in db [:prefs :reduced-motion?])))
(rf/reg-sub :content-status (fn [db _] (get-in db [:content :status])))
(rf/reg-sub :content-error (fn [db _] (get-in db [:content :error])))

(rf/reg-sub
 :publications
 (fn [db _] (get-in db [:content :items])))

(rf/reg-sub
 :categories
 :<- [:publications]
 (fn [items _]
   (let [cats (->> items
                   (map :category)
                   distinct
                   sort)]
     (into [{:id :all :label "Todas"}]
           (map (fn [c] {:id c :label (name c)}) cats)))))

(rf/reg-sub
 :all-tags
 :<- [:publications]
 (fn [items _]
   (->> items
        (mapcat :tags)
        distinct
        sort)))

(rf/reg-sub
 :publication-by-slug
 :<- [:publications]
 (fn [items [_ slug]]
   (some #(when (= slug (:slug %)) %) items)))

(rf/reg-sub
 :filtered-publications
 :<- [:publications]
 :<- [:filters]
 (fn [[items {:keys [tag category]}] _]
   (->> items
        (filter (fn [item]
                  (and (if tag (some #{tag} (:tags item)) true)
                       (if (= category :all) true (= category (:category item))))))
        (sort-by :date #(compare %2 %1)))))
