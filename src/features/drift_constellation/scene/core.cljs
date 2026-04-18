(ns features.drift-constellation.scene.core
  (:require ["three" :as three]
            ["three/examples/jsm/controls/OrbitControls.js" :refer [OrbitControls]]
            [features.drift-constellation.scene.nodes :as nodes]
            [features.drift-constellation.scene.edges :as edges]
            [features.drift-constellation.scene.effects :as effects]
            [features.drift-constellation.scene.interaction :as interaction]))

(defn- clear-group! [group]
  (doseq [child (array-seq (.-children group))]
    (.remove group child)))

(defn- build-graph! [{:keys [node-group edge-group hotspot-group]} {:keys [nodes edges hotspots]}]
  (clear-group! node-group)
  (clear-group! edge-group)
  (clear-group! hotspot-group)
  (let [node-map (into {} (map (juxt :id identity) nodes))
        node-meshes (mapv (fn [node]
                            (let [mesh (nodes/build-node-mesh node)]
                              (.add node-group mesh)
                              mesh))
                          nodes)]
    (doseq [edge edges
            :let [source-node (get node-map (:source edge))
                  target-node (get node-map (:target edge))]
            :when (and source-node target-node)]
      (.add edge-group (edges/build-edge-line source-node target-node edge)))
    (doseq [hotspot hotspots
            :let [members (keep node-map (:resource-ids hotspot))]
            :when (seq members)]
      (.add hotspot-group (effects/build-hotspot members hotspot)))
    node-meshes))

(defn create-scene! [container on-select]
  (let [width (max 320 (.-clientWidth container))
        height (max 320 (.-clientHeight container))
        scene (three/Scene.)
        camera (three/PerspectiveCamera. 62 (/ width height) 0.1 1000)
        renderer (three/WebGLRenderer. (clj->js {:antialias true :alpha true}))
        controls (OrbitControls. camera (.-domElement renderer))
        node-group (three/Group.)
        edge-group (three/Group.)
        hotspot-group (three/Group.)
        frame-id (atom nil)
        node-meshes* (atom [])
        remove-interaction* (atom nil)
        selected-id* (atom nil)
        paused?* (atom false)]
    (.setPixelRatio renderer (min 2 (.-devicePixelRatio js/window)))
    (.setSize renderer width height)
    (.appendChild container (.-domElement renderer))
    (.set (.-position camera) 0 2 13)

    (set! (.-enableDamping controls) true)
    (set! (.-dampingFactor controls) 0.06)
    (set! (.-minDistance controls) 4)
    (set! (.-maxDistance controls) 32)

    (set! (.-background scene) (three/Color. 0x0f1014))
    (.add scene (three/AmbientLight. 0xffffff 0.52))
    (let [key-light (three/DirectionalLight. 0xfff6dd 1.15)
          fill-light (three/DirectionalLight. 0x7aa3ff 0.35)]
      (.set (.-position key-light) 5 10 8)
      (.set (.-position fill-light) -7 4 -6)
      (.add scene key-light)
      (.add scene fill-light))

    (.add scene edge-group)
    (.add scene hotspot-group)
    (.add scene node-group)

    (letfn [(highlight-selected! []
              (doseq [mesh @node-meshes*]
                (let [node-id (aget (.-userData mesh) "node-id")
                      selected? (= node-id @selected-id*)
                      scale (if selected? 1.25 1.0)]
                  (.set (.-scale mesh) scale scale scale))))
            (animate! [t]
              (when-not @paused?*
                (let [time (/ t 1000)]
                  (doseq [mesh @node-meshes*]
                    (let [user-data (.-userData mesh)
                          drift (aget user-data "drift-score")
                          base-y (aget user-data "base-y")
                          status (aget user-data "status")
                          base-emissive (aget user-data "base-emissive")
                          pulse (+ 1 (* 0.12 drift (js/Math.sin (* (+ 1 (* 4 drift)) time))))
                          jitter (if (= status "critical")
                                   (* 0.06 drift (js/Math.sin (* 40 time)))
                                   0)]
                      (set! (.. mesh -position -y) (+ base-y (* 0.15 pulse) jitter))
                      (set! (.. mesh -material -emissiveIntensity) (+ base-emissive (* 0.35 drift (js/Math.abs (js/Math.sin (* 2 time))))))))
                  (doseq [halo (array-seq (.-children hotspot-group))]
                    (let [base (aget (.-userData halo) "base-opacity")
                          intensity (aget (.-userData halo) "intensity")
                          alpha (+ base (* 0.08 intensity (js/Math.abs (js/Math.sin (* 1.8 time)))))
                          s (+ 1 (* 0.05 (js/Math.sin (* 1.2 time))))]
                      (set! (.. halo -material -opacity) alpha)
                      (.set (.-scale halo) s s s)))))
              (.update controls)
              (.render renderer scene camera)
              (reset! frame-id (js/requestAnimationFrame animate!)))
            (resize! []
              (let [w (max 320 (.-clientWidth container))
                    h (max 320 (.-clientHeight container))]
                (.setSize renderer w h)
                (set! (.-aspect camera) (/ w h))
                (.updateProjectionMatrix camera)))]
      (let [resize-handler (fn [] (resize!))]
        (.addEventListener js/window "resize" resize-handler)
        (reset! frame-id (js/requestAnimationFrame animate!))
        {:set-data! (fn [scene-data]
                      (reset! node-meshes* (build-graph! {:node-group node-group
                                                          :edge-group edge-group
                                                          :hotspot-group hotspot-group}
                                                         scene-data))
                      (when-let [remove-listener @remove-interaction*]
                        (remove-listener))
                      (reset! remove-interaction*
                              (interaction/bind-picking! {:renderer renderer
                                                          :camera camera
                                                          :node-meshes @node-meshes*
                                                          :on-select on-select}))
                      (highlight-selected!))
         :set-selected! (fn [node-id]
                          (reset! selected-id* node-id)
                          (highlight-selected!))
         :set-paused! (fn [paused?] (reset! paused?* paused?))
         :reset-camera! (fn []
                          (.set (.-position camera) 0 2 13)
                          (.set (.-target controls) 0 0 0)
                          (.update controls))
         :dispose! (fn []
                     (js/cancelAnimationFrame @frame-id)
                     (when-let [remove-listener @remove-interaction*]
                       (remove-listener))
                     (.removeEventListener js/window "resize" resize-handler)
                     (.dispose controls)
                     (.dispose renderer)
                     (when-let [canvas (.-domElement renderer)]
                       (when (= (.-parentNode canvas) container)
                         (.removeChild container canvas))))}))))
