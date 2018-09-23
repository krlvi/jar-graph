(ns jar-graph.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:import org.openide.util.Lookup)
  (:import org.gephi.project.api.ProjectController)
  (:import org.gephi.io.importer.api.ImportController)
  (:import org.gephi.io.importer.api.EdgeDirectionDefault)
  (:import org.gephi.io.processor.plugin.DefaultProcessor)
  (:import org.gephi.io.exporter.api.ExportController)
  (:import org.gephi.graph.api.GraphModel)
  (:import org.gephi.graph.api.GraphController)
  (:import org.gephi.layout.plugin.AutoLayout)
  (:import java.util.concurrent.TimeUnit)
  (:import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout)
  (:import org.gephi.appearance.api.AppearanceController)
  (:import org.gephi.appearance.api.AppearanceModel)
  (:import org.gephi.appearance.api.AppearanceModel$GraphFunction)
  (:import org.gephi.appearance.plugin.RankingNodeSizeTransformer)
  (:import org.gephi.statistics.plugin.Modularity)
  (:import org.gephi.appearance.plugin.PartitionElementColorTransformer)
  (:import org.gephi.appearance.plugin.palette.PaletteManager)
  (:gen-class))

(def default-file-path "/Users/kiril/dot/libmain-lib.jar.dot")

(defn get-workspace []
  (let [pc (.. (Lookup/getDefault) (lookup ProjectController))]
    (.. pc (newProject))
    (.. pc (getCurrentWorkspace))))

(defn get-import-controller []
  (.. (Lookup/getDefault) (lookup ImportController)))

(defn get-container [file-path import-controller]
  (let [file (java.io.File. file-path)
        container (.. import-controller (importFile file))]
    (.. container (getLoader) (setEdgeDefault EdgeDirectionDefault/DIRECTED))
    (.. container (getLoader) (setAllowAutoNode false))
    container))

(defn import-to-workspace [workspace file-path]
  (let [import-controller (get-import-controller)
        container (get-container file-path import-controller)
        processor (new DefaultProcessor)]
    (.. import-controller (process container processor workspace))))

(defn export-to-pdf [workspace]
  (let [export-controller (.. (Lookup/getDefault) (lookup ExportController))
        pdf-exporter (.. export-controller (getExporter "pdf"))
        baos (new java.io.ByteArrayOutputStream)
        out-file (new java.io.FileOutputStream "out.pdf")]
    (.. pdf-exporter (setPageSize com.itextpdf.text.PageSize/A0))
    (.. pdf-exporter (setWorkspace workspace))
    (.. export-controller (exportStream baos pdf-exporter))
    (.. out-file (write (.. baos (toByteArray))))
    (.. baos (close))
    (.. out-file (close))))

(defn apply-layout []
  (let [graph-model (.. (Lookup/getDefault) (lookup GraphController) (getGraphModel))
        auto-layout (new AutoLayout 5 TimeUnit/SECONDS)
        force-atlas (new ForceAtlasLayout nil)
        prop-adjust-by-size (AutoLayout/createDynamicProperty "forceAtlas.adjustSizes.name" Boolean/TRUE 1.0)]
    (.. auto-layout (setGraphModel graph-model))
    (.. auto-layout (addLayout force-atlas 1.0 (into-array [prop-adjust-by-size])))
    (.. auto-layout (execute))))

(defn apply-size-by-degree []
  (let [appearance-controller (.. (Lookup/getDefault) (lookup AppearanceController))
        appearance-model (.. appearance-controller (getModel))
        graph (.. (Lookup/getDefault) (lookup GraphController) (getGraphModel) (getDirectedGraph))
        degree-ranking (.. appearance-model (getNodeFunction graph AppearanceModel$GraphFunction/NODE_DEGREE,
                                                             RankingNodeSizeTransformer))]
    (.. degree-ranking (getTransformer) (setMinSize 8))
    (.. degree-ranking (getTransformer) (setMaxSize 40))
    (.. appearance-controller (transform degree-ranking))))

(defn apply-color-by-partition []
  (.. (new Modularity) (execute (.. (Lookup/getDefault) (lookup GraphController) (getGraphModel))))
  (let [appearance-controller (.. (Lookup/getDefault) (lookup AppearanceController))
        graph-model (.. (Lookup/getDefault) (lookup GraphController) (getGraphModel))
        mod-column (.. graph-model (getNodeTable) (getColumn Modularity/MODULARITY_CLASS))
        node-func (.. appearance-controller (getModel) (getNodeFunction (.. graph-model (getDirectedGraph))
                                                                          mod-column PartitionElementColorTransformer))
        partition (.. node-func (getPartition))
        palette (.. (PaletteManager/getInstance) (randomPalette (.. partition (size))))
        ]
    (.. partition (setColors (.. palette (getColors))))
    (.. appearance-controller (transform node-func))))

(defn dowork [dot-file]
  (let [workspace (get-workspace)]
    (import-to-workspace workspace dot-file)
    (apply-size-by-degree)
    (apply-color-by-partition)
    (apply-layout)
    (export-to-pdf workspace)))

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
      (:dot-file options) (dowork (first arguments)))))
