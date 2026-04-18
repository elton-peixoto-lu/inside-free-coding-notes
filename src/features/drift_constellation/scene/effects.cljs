(ns features.drift-constellation.scene.effects
  (:require ["three" :as three]))

(defn build-hotspot [resource-nodes {:keys [id intensity]}]
  (let [count (max 1 (count resource-nodes))
        [sx sy sz] (reduce (fn [[ax ay az] {:keys [position]}]
                             (let [[x y z] position]
                               [(+ ax x) (+ ay y) (+ az z)]))
                           [0 0 0]
                           resource-nodes)
        cx (/ sx count)
        cy (/ sy count)
        cz (/ sz count)
        radius (+ 1.2 (* 2.2 intensity))
        geometry (three/SphereGeometry. radius 24 24)
        material (three/MeshBasicMaterial.
                  (clj->js {:color 0xbe5a2b
                            :transparent true
                            :opacity (+ 0.06 (* 0.15 intensity))
                            :wireframe false}))
        mesh (three/Mesh. geometry material)]
    (.set (.-position mesh) cx cy cz)
    (aset mesh "userData" (clj->js {:hotspot-id id
                                    :intensity intensity
                                    :base-opacity (+ 0.06 (* 0.15 intensity))}))
    mesh))
