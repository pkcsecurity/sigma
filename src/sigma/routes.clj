(ns sigma.routes
  (:require [compojure.core :as r]
            [compojure.route :as croute]
            [sigma.utils :as u]
            [sigma.routes.init]))

(r/defroutes routes
  (r/GET "/" [] "info")
  (r/GET "/CA" [] "Hello, I am the cert auth")
  (r/GET "/keys" [] "keys")
  (r/POST "/init" [] sigma.routes.init/init)
  (r/POST "/finalize" [] sigma.routes.init/finalize-connection)
  (r/POST "/identity" [] "identify")
  (r/POST "/message" [] "message")
  (croute/not-found nil))
