(ns sigma.client
  (:require [clj-http.client :as http]
            [msgpack.core :as msg]
            [sigma.utils :as u]
            [buddy.core.codecs :refer :all]
            [caesium.crypto.scalarmult :as sm]
            [buddy.core.hash :as hash]
            [buddy.core.kdf :as kdf]
            [caesium.crypto.secretbox :as sbox]
            [buddy.core.mac :as mac]
            [caesium.crypto.sign :as sign]
            [sigma.routes.ca :as ca]))

(def client-info
  (atom {:sign nil}))

(defn first-step []
  (let [{:keys [public secret]} (u/gen-keys)]
    (swap! client-info assoc-in [:key-exchange :public] public)
    (swap! client-info assoc-in [:key-exchange :secret] secret))
  
  (let [{:keys [body] :as req} (http/post "http://127.0.0.1:8080/init"
                                          {:body (msg/pack {:gx (.array (get-in @client-info [:key-exchange :public]))
                                                            :nonce 0})
                                           :content-type :byte-array
                                           :as :byte-array})
        resp (msg/unpack body)]
    (println "client: received init back from server")
    (println resp)
    resp))

(defn second-step [ke km n]
  (let [id "A"
        gxgy {:gx (.array (get-in @client-info [:key-exchange :public]))
              :gy (get @client-info :gy)}
        mac-a (mac/hash id {:key km
                            :alg :hmac+sha256})
        sig (sign/signed (msg/pack gxgy) (:sign @client-info))
        whole-message (msg/pack {:id id
                                 :sig-a sig
                                 :mac-km mac-a})
        enc (sbox/encrypt ke (sbox/int->nonce (inc n)) whole-message)
        {:keys [body] :as req} (http/post "http://127.0.0.1:8080/finalize" {:body (msg/pack {:enc enc
                                                                                             :nonce (inc n)})
                                                                            :content-type :byte-array
                                                                            :as :byte-array})]
    (msg/unpack body)))

(defn steps []
  (println "CLIENT: sending my gx to SERVER")
  (let [{:keys [gy nonce] :as init-res} (first-step)
        shared-secret (hash/sha256 (sm/scalarmult (get-in @client-info [:key-exchange :secret]) gy))
        ke shared-secret
        km (kdf/get-bytes (kdf/engine {:alg :hkdf
                                       :digest :blake2b-512
                                       :key shared-secret}) 32)
        {{:keys [mac-km sig-b id]} :enc :as decrypt-res} (update init-res :enc #(msg/unpack (sbox/decrypt ke (sbox/int->nonce nonce) %)))
        mac-valid? (mac/verify id mac-km {:key km :alg :hmac+sha256})
        verify-sig (sign/verify sig-b (get @ca/certificates id))]
    (swap! client-info assoc :gy gy)
    (swap! client-info assoc :gxy shared-secret)
    (swap! client-info assoc :ke ke)
    (swap! client-info assoc :km km)
    (println "client: shared secret is " (bytes->hex shared-secret))
    (println decrypt-res)
    (second-step ke km nonce)))
