(ns jar-graph.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [jar-graph.graph :as graph])
  (:gen-class))

(def default-file-path "/Users/kiril/dot/libcommon-lib.jar.dot")

(defn exit [msg]
  (println msg)
  (System/exit 0))

(def cli-options
  [["-d" "--dot-file" "Path to the dot graph file to analyze"]
   ["-h" "--help" "Prints this help message"]])

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (clojure.tools.cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit summary)
      errors (exit errors)
      (:dot-file options) (println (first arguments)))))
