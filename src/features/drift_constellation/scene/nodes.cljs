(ns features.drift-constellation.scene.nodes
  (:require ["three" :as three]))

(def severity-color
  {:healthy 0x4a7c59
   :attention 0xc2943d
   :high 0xd36a2e
   :critical 0xc13a32})

(defn node-size [{:keys [drift-score]}]
  (+ 0.15 (* 0.45 drift-score)))

(defn build-node-mesh [node]
  (let [radius (node-size node)
        geometry (three/SphereGeometry. radius 20 20)
        color (get severity-color (:severity node) 0x7f8c8d)
        material (three/MeshStandardMaterial.
                  (clj->js {:color color
                            :emissive color
                            :emissiveIntensity (+ 0.12 (* 0.6 (:drift-score node)))
                            :roughness 0.3
                            :metalness 0.15}))
        mesh (three/Mesh. geometry material)
        [x y z] (:position node)]
    (.set (.-position mesh) x y z)
    (aset mesh "userData" (clj->js {:node-id (:id node)
                                    :base-y y
                                    :drift-score (:drift-score node)
                                    :severity (name (:severity node))
                                    :status (name (:status node))
                                    :base-emissive (+ 0.12 (* 0.6 (:drift-score node)))}))
    mesh))
