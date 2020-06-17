(ns pinkgorilla.nrepl.ws.httpkit-ws-relay
  "A websocket handler that passes messages back and forth to an already running nREPL server."
  ;(:use [compojure.core :only (defroutes GET)]
  ;      ring.util.response
  ;      ring.middleware.cors)
  (:require
   [taoensso.timbre :refer [debug info error]]
   [clojure.edn :as edn]
   [org.httpkit.server :as http]
   [nrepl.server]
   [nrepl.core]
   [nrepl.transport]
   [pinkgorilla.nrepl.ws.relay :refer [on-ws-receive]]
   [pinkgorilla.nrepl.middleware.cider :refer [cider-handler]]))

(def clients (atom {}))
(def my-cider-handler (cider-handler))

; this causes lint errors, and is depreciated
; https://github.com/http-kit/http-kit/blob/master/src/org/httpkit/server.clj

#_(defn ws-handler
    [req]
    (http/with-channel req con
      (println "nrepl relay: ws-client connected! conn: " con)
      (let [transport (nrepl.transport/piped-transports)]
        (swap! clients assoc con transport)
        (http/on-receive con (partial on-ws-receive
                                      my-cider-handler transport
                                      http/send! con))
        (http/on-close con (fn [status]
                             (swap! clients dissoc con)
                             (println "nrepl relay: ws-client disconnected. status: " status "con:" con))))))

(defn ws-handler [ring-req]
  (http/as-channel ring-req
                   {:on-open    (fn [ch]
                                  (println "nrepl relay: ws-client connected! conn: " ch)
                                  (let [transport (nrepl.transport/piped-transports)]
                                  ;(swap! clients_ conj ch)
                                    (swap! clients assoc ch transport)))
                    :on-receive (fn [ch message]
                                  (let [transport (ch @clients)]
                                    (on-ws-receive my-cider-handler transport
                                                   http/send! ch message)))
                    :on-ping     (fn [ch data]
                                   (println "ws ping received"))
                    :on-close    (fn [ch status]
                                   (swap! clients dissoc ch)
                                   (println "nrepl relay: ws-client disconnected. status: " status "con:" ch))}))

;; ASYNC IO based websocket handler:
;(defn handler
;  [request]
;  (let [c (process request)] ;; long running process that returns a channel
;    (http/with-channel request channel
;      (http/send! channel {:status 200
;                           :body (<!! (go (<! c)))))
;      (http/on-close channel (fn [_] (async/close! c))))))


; send heartbeats to all connected websockets.


#_(future
    (loop []
      (doseq [client @clients]
        (http/send! (key client)
                    (pr-str {:heartbeat (rand 10)})
                    false))
      (Thread/sleep 10000)
      (recur)))
