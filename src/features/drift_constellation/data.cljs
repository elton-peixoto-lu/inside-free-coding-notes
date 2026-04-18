(ns features.drift-constellation.data)

(def mock-graph
  {:nodes [{:id "eks-1"
            :label "EKS Cluster"
            :type :cluster
            :position [2 5 1]
            :drift-score 0.82
            :cost-score 0.45
            :risk-score 0.71
            :status :critical
            :desired {:version "1.29" :nodes 3}
            :observed {:version "1.28" :nodes 5}}
           {:id "payments-ns"
            :label "payments"
            :type :namespace
            :position [4 3 2]
            :drift-score 0.32
            :cost-score 0.22
            :risk-score 0.30
            :status :warning
            :desired {:replicas 2}
            :observed {:replicas 3}}
           {:id "ingress-gw"
            :label "Ingress Gateway"
            :type :network
            :position [0 4 -2]
            :drift-score 0.57
            :cost-score 0.51
            :risk-score 0.63
            :status :high
            :desired {:replicas 2 :policy "strict"}
            :observed {:replicas 3 :policy "permissive"}}
           {:id "rds-orders"
            :label "RDS Orders"
            :type :database
            :position [-3 2 1]
            :drift-score 0.14
            :cost-score 0.56
            :risk-score 0.21
            :status :healthy
            :desired {:storage-gb 80 :backup-retention-days 7}
            :observed {:storage-gb 80 :backup-retention-days 7}}
           {:id "workers-batch"
            :label "Batch Workers"
            :type :workload
            :position [-1 1 -3]
            :drift-score 0.91
            :cost-score 0.72
            :risk-score 0.86
            :status :critical
            :desired {:replicas 4 :image "sha256:aaa"}
            :observed {:replicas 9 :image "sha256:ddd"}}]
   :edges [{:source "eks-1" :target "payments-ns" :type :dependency :conflict false}
           {:source "eks-1" :target "ingress-gw" :type :dependency :conflict true}
           {:source "payments-ns" :target "rds-orders" :type :dependency :conflict false}
           {:source "payments-ns" :target "workers-batch" :type :dependency :conflict true}
           {:source "workers-batch" :target "rds-orders" :type :traffic :conflict false}]
   :hotspots [{:id "zone-a"
               :resource-ids ["eks-1" "payments-ns" "ingress-gw"]
               :intensity 0.77}
              {:id "zone-b"
               :resource-ids ["workers-batch"]
               :intensity 0.92}]})

(def severity-order [:all :healthy :attention :high :critical])

(defn drift->severity [drift-score]
  (cond
    (<= drift-score 0.2) :healthy
    (<= drift-score 0.5) :attention
    (<= drift-score 0.8) :high
    :else :critical))

(defn distinct-types [nodes]
  (->> nodes (map :type) distinct sort))
