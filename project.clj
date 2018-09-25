(require 'cemerick.pomegranate.aether)

(cemerick.pomegranate.aether/register-wagon-factory!
  "http" #(org.apache.maven.wagon.providers.http.HttpWagon.))

(defproject jar-graph "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.gephi/gephi-toolkit "0.9.2"]
                 [com.stuartsierra/frequencies "0.1.0"]]
  :main ^:skip-aot jar-graph.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
