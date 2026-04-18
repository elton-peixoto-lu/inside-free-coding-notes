(ns features.drift-constellation.scene.interaction
  (:require ["three" :as three]))

(defn bind-picking! [{:keys [renderer camera node-meshes on-select]}]
  (let [raycaster (three/Raycaster.)
        pointer (three/Vector2.)
        dom-el (.-domElement renderer)
        click-handler
        (fn [event]
          (let [rect (.getBoundingClientRect dom-el)
                nx (- (/ (- (.-clientX event) (.-left rect)) (.-width rect)) 0.5)
                ny (- (/ (- (.-clientY event) (.-top rect)) (.-height rect)) 0.5)]
            (set! (.-x pointer) (* 2 nx))
            (set! (.-y pointer) (* -2 ny))
            (.setFromCamera raycaster pointer camera)
            (let [intersections (.intersectObjects raycaster (clj->js node-meshes) false)]
              (when (> (.-length intersections) 0)
                (let [hit (aget intersections 0)
                      object (.-object hit)
                      node-id (aget (.-userData object) "node-id")]
                  (when node-id (on-select node-id)))))))]
    (.addEventListener dom-el "click" click-handler)
    (fn []
      (.removeEventListener dom-el "click" click-handler))))
