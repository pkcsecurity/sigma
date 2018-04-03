(ns sigma.routes.init
    (:require [msgpack.core :as msg]
              [msgpack.clojure-extensions]
              [sigma.utils :as u]))

(defn init [{:keys [body] :as req}]
  (let [unpacked-req (msg/unpack body)
        keys (u/gen-keys)]
    (println keys)
    (u/print-key (:secret keys))
    (u/print-key (:public keys))))