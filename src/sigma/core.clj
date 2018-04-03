(ns sigma.core
  (:gen-class)
  (:require [immutant.web :as server]
            [ring.middleware.file :as file]
            [ring.middleware.params :as params]
            [ring.middleware.keyword-params :as kw-params]
            [ring.middleware.content-type :as content-type]
            [sigma.roles :as roles]
            [sigma.routes :as r]
            [sigma.properties :as p]
            [sigma.utils :as u]
            [clj-http.client :as http]))
          
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

(defn -main []
  ;(init)
  (if p/prod?
    (server/run app
      :host "0.0.0.0"
      :port "8080")
    (server/run-dmc app
      :host "127.0.0.1"
      :port "8080")))