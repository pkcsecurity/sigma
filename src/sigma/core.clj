(ns sigma.core
  (:gen-class)
  (:require [immutant.web :as server]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.keyword-params :as kw-params]
            [ring.middleware.params :as params]
            [sigma.properties :as p]
            [sigma.roles :as roles]
            [sigma.routes :as r]
            [sigma.routes.ca :as ca]
            [sigma.server :as s-server]
            [sigma.client :as client]
            [buddy.core.codecs :refer :all]
            [caesium.crypto.sign :as sign]))

(defn client-server-status []
  {:client @client/client-info
   :server @s-server/server-info})

(defn client-server-shared-secret-keys []
  (let [{:keys [client server]} (client-server-status)]
    {:client (bytes->hex (:gxy client))
     :server (bytes->hex (:gxy server))}))

(defn wrap-ignore-trailing-slash [handler]
  (fn [request]
    (let [uri (:uri request)]
      (handler (assoc request
                 :uri
                 (if (and
                       (.endsWith uri "/")
                       (not= "/" uri))
                   (subs uri 0 (dec (count uri)))
                   uri))))))

(def app
  (-> r/routes
    (roles/wrap-security)
    ;(file/wrap-file "static" {:index-files? false})
    (content-type/wrap-content-type)
    (kw-params/wrap-keyword-params)
    (params/wrap-params)
    (wrap-ignore-trailing-slash)
    ;(spec/wrap-conform-failure)
    ))

(defn init-certs []
  (let [a (sign/keypair!)
        b (sign/keypair!)]
    (swap! ca/certificates assoc "A" (:public a))
    (swap! client/client-info assoc :sign (:secret a))
    (swap! ca/certificates assoc "B" (:public b))
    (swap! s-server/server-info assoc :sign (:secret b))))

(defn -main []
  (init-certs)
  (if p/prod?
    (server/run app
      :host "0.0.0.0"
      :port "8080")
    (server/run-dmc app
      :host "127.0.0.1"
      :port "8080")))
