(ns sigma.utils
  (:require [buddy.core.hash :as hash]
            [buddy.core.codecs :refer :all]
            [caesium.crypto.box :as box]
            [caesium.crypto.scalarmult :as sm]
            [caesium.crypto.sign :as sign]
            [msgpack.core :as msg]
            [msgpack.clojure-extensions]))

(def visitor-data (atom {}))

(defn http-ok [mp]
  {:status 200 :body (java.io.ByteArrayInputStream. (msg/pack mp))})

(defprotocol KeyPrettyPrint
  (key->hex-string [obj]))

(extend-protocol KeyPrettyPrint
  (class (byte-array []))
  (key->hex-string [obj] (bytes->hex obj))
  java.nio.ByteBuffer
  (key->hex-string [obj] (bytes->hex (.array obj))))

(defn gen-keys []
  (box/keypair!))

(defn gen-shared-key [private-key public-key]
  (hash/sha256
    (sm/scalarmult private-key public-key)))

; (:require [clojure.tools.logging :as log]
;             [sigma-chat.common.utils :as utils]
;             [clj-http.util :refer [opt] :as http]
;             [caesium.crypto.box :as box]
;             [caesium.crypto.sign :as sign]
;             [caesium.byte-bufs :as bb]
            
;             [cheshire.core :refer [parse-string generate-string]]
;             [buddy.core.codecs :as codecs]
;             [buddy.core.kdf :as kdf]
;             [buddy.core.mac :as mac]
;             [buddy.core.nonce :as nonce]
;             [sigma-chat.common.properties :as props]))

; (defn gen-keys
;   "Generates a private, public, and shared key using caesium box and scalar multiplication.

;   Keys are stored in vault under the id as secret, public, and shared. Human readable hex values are
;   stored as secret-prt, public-prt, shared-prt. If a seed is passed, it is also stored
;   in the vault as seed and seed-prt under id for the raw and hex values respectively."
;   ([id key]
;   (let [keys (box/keypair!)
;         shared (sm/scalarmult (get keys :secret) key)
;         shared-prt (utils/byte-arr->hex-string shared)
;         secret-prt (utils/byte-buf->hex-string (get keys :secret))
;         public-prt (utils/byte-buf->hex-string (get keys :public))
;         keys (assoc keys :secret-prt secret-prt :public-prt public-prt
;                           :shared shared :shared-prt shared-prt)]
;     (swap! vault assoc id keys)
;     (log/info "SERVER: Generated secret key: " secret-prt)
;     (log/info "SERVER: Generated public key: " public-prt)
;     (log/info "SERVER: Generated shared key: " shared-prt)))
;   ([id key seed]
;   (let [keys (box/keypair! seed)
;         shared (sm/scalarmult (get keys :secret) key)
;         shared-prt (utils/byte-buf->hex-string shared)
;         secret-prt (utils/byte-buf->hex-string (get keys :secret))
;         public-prt (utils/byte-buf->hex-string (get keys :public))
;         seed (bb/->indirect-byte-buf seed)
;         seed-prt (utils/byte-buf->hex-string seed)
;         keys (assoc keys :secret-prt secret-prt :public-prt public-prt
;                           :shared shared :shared-prt shared-prt
;                           :seed seed :seed-prt seed-prt)]
;     (swap! vault assoc id keys)
;     (log/info "SERVER: Generated secret key with seed [" seed-prt "]: " secret-prt)
;     (log/info "SERVER: Generated public key with seed [" seed-prt "]: " public-prt)
;     (log/info "SERVER: Generated shared key with seed [" seed-prt "]: " shared-prt))))
