(ns jar-graph.graph
  (:require [com.stuartsierra.frequencies :as freq])
  (:import [java.util.concurrent TimeUnit]
           [org.openide.util Lookup]
           [org.gephi.project.api ProjectController]
           [org.gephi.io.importer.api ImportController EdgeDirectionDefault]
           [org.gephi.io.processor.plugin DefaultProcessor]
           [org.gephi.io.exporter.api ExportController]
           [org.gephi.graph.api GraphController]
           [org.gephi.layout.plugin AutoLayout]
           [org.gephi.layout.plugin.forceAtlas ForceAtlasLayout]
           [org.gephi.appearance.api AppearanceController AppearanceModel$GraphFunction]
           [org.gephi.appearance.plugin RankingNodeSizeTransformer PartitionElementColorTransformer]
           [org.gephi.statistics.plugin Modularity]
           [org.gephi.layout.plugin.noverlap NoverlapLayout]
           [org.gephi.preview.api PreviewController PreviewProperty]
           [org.gephi.layout.plugin.labelAdjust LabelAdjust]
           [org.gephi.preview.types DependantOriginalColor]
           [org.gephi.appearance.plugin.palette PaletteManager])
  (:gen-class))

(defn- look-up [class]
  (.. (Lookup/getDefault) (lookup class)))

(defn- graph-model []
  (.. (look-up GraphController) (getGraphModel)))

(defn- workspace []
  (let [pc (look-up ProjectController)
        ws (.. pc (getCurrentWorkspace))]
    (if ws
      ws
      (do
        (.. pc (newProject))
        (.. pc (getCurrentWorkspace))))))

(defn- get-container [file-path import-controller]
  (let [file (java.io.File. file-path)
        container (.. import-controller (importFile file))]
    (.. container (getLoader) (setEdgeDefault EdgeDirectionDefault/DIRECTED))
    (.. container (getLoader) (setAllowAutoNode false))
    container))

(defn- import-to-workspace [file-path]
  (let [import-controller (look-up ImportController)
        container (get-container file-path import-controller)
        processor (new DefaultProcessor)]
    (.. import-controller (process container processor (workspace)))))

(defn- gen-stats []
  (let [graph (.. (graph-model) (getDirectedGraph))
        nodes (.. graph (getNodes) (toCollection))
        in-degree (map (fn [n] (.. graph (getInDegree n))) nodes)
        out-degree (map (fn [n] (.. graph (getOutDegree n))) nodes)
        degree (map (fn [n] (.. graph (getDegree n))) nodes)]
    {:in-degree (freq/stats (frequencies in-degree))
     :out-degree (freq/stats (frequencies out-degree))
     :degree (freq/stats (frequencies degree))}))

(defn- indegree-minus-outdegree-list []
  (let [graph (.. (graph-model) (getDirectedGraph))
        nodes (.. graph (getNodes) (toCollection))]
    (sort-by :in-minus-out
    (map (fn [n]
           {:name         (.getLabel n)
            :in-minus-out (- (.. graph (getInDegree n)) (.. graph (getOutDegree n)))})
         nodes))))

(defn- apply-size-by-degree [min-size max-size]
  (let [appearance-controller (look-up AppearanceController)
        appearance-model (.. appearance-controller (getModel))
        graph (.. (graph-model) (getDirectedGraph))
        degree-ranking (.. appearance-model (getNodeFunction graph AppearanceModel$GraphFunction/NODE_INDEGREE,
                                                             RankingNodeSizeTransformer))]
    (.. degree-ranking (getTransformer) (setMinSize min-size))
    (.. degree-ranking (getTransformer) (setMaxSize max-size))
    (.. appearance-controller (transform degree-ranking))))

(defn- apply-color-by-partition []
  (.. (new Modularity) (execute (graph-model)))
  (let [appearance-controller (look-up AppearanceController)
        mod-column (.. (graph-model) (getNodeTable) (getColumn Modularity/MODULARITY_CLASS))
        node-func (.. appearance-controller (getModel) (getNodeFunction (.. (graph-model) (getDirectedGraph))
                                                                        mod-column PartitionElementColorTransformer))
        partition (.. node-func (getPartition))
        palette (.. (PaletteManager/getInstance) (randomPalette (.. partition (size))))
        ]
    (.. partition (setColors (.. palette (getColors))))
    (.. appearance-controller (transform node-func))))

(defn- apply-labels [font-size dark-mode]
  (let [props (.. (.. (look-up PreviewController) (getModel)) (getProperties))]
    (.. props (putValue PreviewProperty/DIRECTED Boolean/TRUE))
    (.. props (putValue PreviewProperty/EDGE_CURVED Boolean/FALSE))
    (.. props (putValue PreviewProperty/SHOW_NODE_LABELS Boolean/TRUE))
    (.. props (putValue PreviewProperty/NODE_LABEL_PROPORTIONAL_SIZE Boolean/FALSE))
    (.. props (putValue PreviewProperty/EDGE_THICKNESS 0.3))
    (.. props (putValue PreviewProperty/EDGE_OPACITY 85))
    (if dark-mode
      (do
        (.. props (putValue PreviewProperty/BACKGROUND_COLOR java.awt.Color/BLACK))
        (.. props (putValue PreviewProperty/NODE_LABEL_COLOR (new DependantOriginalColor java.awt.Color/WHITE)))))
    (.. props (putValue PreviewProperty/NODE_LABEL_FONT (new java.awt.Font nil 0 font-size)))
    (.. (look-up PreviewController) (refreshPreview))))

(defn- apply-layout [time-sec repulsion-strength]
  (let [auto-layout (new AutoLayout time-sec TimeUnit/SECONDS)
        no-overlap (new NoverlapLayout nil)
        force-atlas (new ForceAtlasLayout nil)
        label-adjust (new LabelAdjust nil)
        prop-adjust-by-size (AutoLayout/createDynamicProperty "forceAtlas.adjustSizes.name" Boolean/TRUE 0.0)
        prop-repulsion (AutoLayout/createDynamicProperty "forceAtlas.repulsionStrength.name" (new Double (double repulsion-strength)) 0.0)]
    (.. auto-layout (setGraphModel (graph-model)))
    (.. auto-layout (addLayout force-atlas 0.90 (into-array [prop-adjust-by-size prop-repulsion])))
    (.. auto-layout (addLayout no-overlap 0.05))
    (.. auto-layout (addLayout label-adjust 0.05))
    (.. auto-layout (execute))))

(defn- export-to-pdf [out-file]
  (let [export-controller (look-up ExportController)
        pdf-exporter (.. export-controller (getExporter "pdf"))
        baos (new java.io.ByteArrayOutputStream)
        out-file (new java.io.FileOutputStream out-file)]
    (.. pdf-exporter (setPageSize com.itextpdf.text.PageSize/A0))
    (.. pdf-exporter (setWorkspace (workspace)))
    (.. export-controller (exportStream baos pdf-exporter))
    (.. out-file (write (.. baos (toByteArray))))
    (.. baos (close))
    (.. out-file (close))))

(defn analyze [options]
  (let [{:keys [dot-file skip-graph out-graph simulation-time repulsion-strength min-size max-size label-size
                list-nodes]}
        options]
    (import-to-workspace dot-file)
    (if-not skip-graph
      (do
        (apply-size-by-degree min-size max-size)
        (apply-color-by-partition)
        (apply-labels label-size false)
        (apply-layout simulation-time repulsion-strength)
        (if out-graph
          (export-to-pdf out-graph)
          (export-to-pdf (str dot-file ".pdf")))))
    (if list-nodes
      (indegree-minus-outdegree-list)
      (gen-stats))))
