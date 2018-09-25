(ns jar-graph.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [jar-graph.graph :as graph]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(defn exit [msg]
  (println msg)
  (System/exit 0))

(def cli-options
  [["-d" "--dot-file DOTFILE" "Path to the dot graph file to analyze"
    :required "Provide dot file to analyze"]
   ["-s" "--skip-graph" "Skip drawing graph PDF"]
   ["-o" "--out-graph OUT" "File name of the output graph PDF"]
   ["-t" "--simulation-time TIME" "Duration of clustering simulation in seconds"
    :default 10
    :parse-fn #(Integer/parseInt %)]
   ["-r" "--repulsion-strength REPULSION" "Node repulsion strength"
    :default 800
    :parse-fn #(Integer/parseInt %)]
   ["-n" "--min-size MINSIZE" "Size of the least connected nodes"
    :default 6
    :parse-fn #(Integer/parseInt %)]
   ["-x" "--max-size MAXSIZE" "Size of the most connected nodes"
    :default 40
    :parse-fn #(Integer/parseInt %)]
   ["-l" "--label-size LABELSIZE" "Label font size"
    :default 2
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--help" "Prints this help message"]])

(defn -main [& args]
  (let [{:keys [options errors summary]} (clojure.tools.cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit summary)
      errors (exit errors)
      (:dot-file options) (clojure.pprint/pprint (graph/analyze options))
      :else (exit summary))))
