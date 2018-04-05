(ns sigma.routes.init
  (:require [msgpack.core :as msg]
            [msgpack.clojure-extensions]
            [sigma.utils :as u]
            [caesium.crypto.scalarmult :as sm]
            [buddy.core.codecs :refer :all]
            [buddy.core.hash :as hash]
            [sigma.server :as server]
            [buddy.core.kdf :as kdf]
            [buddy.core.mac :as mac]
            [sigma.routes.ca :as ca]
            [caesium.crypto.sign :as sign]
            [caesium.crypto.secretbox :as sbox]))

(defn init [{:keys [body] :as req}]
  (println "SERVER: new connection initiated!")
  (let [{:keys [gx nonce] :as req} (msg/unpack body)
        {:keys [secret public]} (u/gen-keys)
        shared-secret (hash/sha256 (sm/scalarmult secret gx))
        ke shared-secret
        km (kdf/get-bytes (kdf/engine {:alg :hkdf
                                       :digest :blake2b-512
                                       :key shared-secret}) 32)
        id "B"
        mac-id (mac/hash id {:key km
                             :alg :hmac+sha256})
        sig (sign/signed (msg/pack {:gx gx :gy (.array public)}) (:sign @server/server-info))
        whole-message (msg/pack {:id id
                                 :mac-km mac-id
                                 :sig-b sig})
        enc (sbox/encrypt ke (sbox/int->nonce (inc nonce)) whole-message)]
    (swap! server/server-info assoc-in [:msg] 1)
    (swap! server/server-info assoc-in [:key-exchange :public] public)
    (swap! server/server-info assoc-in [:key-exchange :secret] secret)
    (swap! server/server-info assoc-in [:gxy] shared-secret)
    (swap! server/server-info assoc-in [:ke] ke)
    (swap! server/server-info assoc-in [:km] km)
    (println "server: shared secret is " (bytes->hex shared-secret))
    (println "server: sending gy back to client")
    (u/http-ok {:gy (.array public)
                :enc enc
                :nonce (inc nonce)})))

(defn finalize-connection [{:keys [body] :as req}]
  (let [{:keys [nonce] :as unpacked} (msg/unpack body)
        {:keys [ke km]} @server/server-info
        {{:keys [id sig-a mac-km]} :enc} (update unpacked :enc #(msg/unpack (sbox/decrypt ke (sbox/int->nonce nonce) %)))
        mac-valid? (mac/verify id mac-km {:key km :alg :hmac+sha256})
        verify-sig (sign/verify sig-a (get @ca/certificates id))]
    (println "server: mac valid?" mac-valid?)
    (u/http-ok {:empty "for now"})))
