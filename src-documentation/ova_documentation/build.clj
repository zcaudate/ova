(ns ova-documentation.build
  "Build and check the documentation with code.doc."
  (:require [clojure.java.io :as io]
            [code.doc :as doc]
            [code.doc.check :as check]
            [code.doc.executive :as executive]))

(defn -main
  [& [command]]
  (try
    (let [failures (check/check-failures :all {:eval true})]
      (when (pos? failures)
        (throw (ex-info "Documentation checks failed" {:issues failures})))
      (case command
        "check" (println "Documentation checks passed.")
        nil (let [project (doc/make-project)
                  sites (get-in project [:publish :sites])
                  pages (executive/all-pages project)]
              (doseq [site (keys sites)]
                (when-not (.exists (io/file "template" (name site)))
                  (executive/init-template site {:write true :print {:function false}} sites project))
                (executive/deploy-template site {:write true :print {:function false}} sites project))
              (doseq [page (sort (keys pages))]
                (println (:path (executive/render page {:write true :print {:function false}} pages project)))))
        (throw (ex-info "Use lein docs or lein docs-check" {:command command}))))
    (shutdown-agents)
    (catch Throwable error
      (binding [*out* *err*]
        (println (.getMessage error))
        (when-let [details (ex-data error)] (prn details)))
      (System/exit 1))))
