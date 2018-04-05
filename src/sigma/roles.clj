(ns sigma.roles
  (:require [buddy.auth.accessrules :as authz]
            [clojure.tools.logging :as log]))

(def allow-all (constantly true))
(def deny-all (constantly false))

(def rules
  [{:uris ["/"
           "/CA"
           "/keys"
           "/init"
           "/identify"
           "/message"
           "/finalize"]
    :handler allow-all}

   ;Catch all for any unrecognized calls
   {:pattern #"^.*$"
    :handler deny-all
    :redirect "/"}])

(defn wrap-security [app]
  (-> app
    (authz/wrap-access-rules {:rules rules})))
