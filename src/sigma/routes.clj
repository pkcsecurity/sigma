(ns sigma.routes
  (:require [compojure.core :as r]
            [compojure.route :as croute]
            [sigma.utils :as u]))

(r/defroutes routes
  (r/GET "/" [] "info")
  (r/GET "/CA" [] "Hello, I am the cert auth")
  (r/GET "/keys" [] "keys")
  (r/POST "/init" [] "init")
  (r/POST "/identity" [] "identify")
  (r/POST "/message" [] "message")
  (croute/not-found nil))
