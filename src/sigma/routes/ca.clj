(ns sigma.routes.ca
  (:require [caesium.crypto.sign :as sign]))

(def certificates
  (atom {}))

(defn get-cert-mappings []
  {:status 200 :body {}})
