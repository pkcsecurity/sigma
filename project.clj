(defproject sigma "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot sigma.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :dependencies [[org.clojure/clojure "LATEST"]
                 [org.clojure/tools.logging "LATEST"]
                 [cheshire "LATEST"]
                 [caesium "LATEST"]
                 [clojure-msgpack "LATEST"]
                 [org.immutant/web "LATEST"]
                 [ring/ring-core "LATEST"]
                 [ring/ring-devel "LATEST"]
                 [ring/ring-json "LATEST"]
                 [compojure "LATEST"]
                 [buddy/buddy-auth "LATEST"]
                 [buddy/buddy-sign "LATEST"]
                 [buddy/buddy-hashers "LATEST"]
                 [org.clojure/spec.alpha "LATEST"]
                 [clj-time "LATEST"]
                 [clj-http "LATEST"]])
