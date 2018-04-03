(ns sigma.properties
  (:require [clojure.java.io :as io]))

(defn environment [v] (System/getenv v))

(def props-file (io/resource "properties.edn"))

(def edn-str (delay
               (read-string
                 (if props-file
                   (slurp props-file)
                   (environment "PROPERTIES_EDN")))))

(defn property [& ks] (get-in @edn-str ks))

(def prod?
  (or
    (= :prod (property :mode))
    (= (environment "RING_ENV") "production")))

