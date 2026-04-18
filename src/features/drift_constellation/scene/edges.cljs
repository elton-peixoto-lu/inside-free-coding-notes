(ns features.drift-constellation.scene.edges
  (:require ["three" :as three]))

(defn- to-v3 [[x y z]]
  (three/Vector3. x y z))

(defn build-edge-line [source-node target-node {:keys [conflict]}]
  (let [points (clj->js [(to-v3 (:position source-node))
                         (to-v3 (:position target-node))])
        geometry (three/BufferGeometry.)]
    (.setFromPoints geometry points)
    (let [material (three/LineBasicMaterial.
                    (clj->js {:color (if conflict 0xd24c40 0x7e7a76)
                              :transparent true
                              :opacity (if conflict 0.9 0.45)}))
          line (three/Line. geometry material)]
      (aset line "userData" (clj->js {:conflict conflict}))
      line)))
