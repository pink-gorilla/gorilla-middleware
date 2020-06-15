(ns pinkgorilla.nrepl.server.jee-interop
  ;; (:use compojure.core)
  (:require
   [taoensso.timbre :refer [debug info]]
   ;; [clojure.tools.logging :as log]
   ;; [cheshire.core :as json]
   #_(:refer [clojure.data.json :rename {write-str generate-string
                                         read-str  parse-string}])
   [clojure.data.json :as json]
   [clojure.walk :as w]
   [clojure.pprint :as pp]
   [nrepl.server :as nrepl-server]
   [nrepl.core :as nrepl]
   [nrepl [transport :as transport]]
   [pinkgorilla.nrepl.middleware.cider :as mw-cider]))

(def handler (atom (mw-cider/cider-handler)))

;; TODO unify all the things!
(defn- process-replies
  [reply-fn replies-seq]
  (debug "Process replies")
  (loop [s replies-seq
         result []]
    (let [msg (first s)
          next-result (conj result (reply-fn msg))]
      (if (contains? (:status msg) :done)
        next-result
        (recur (rest s)
               next-result)))))

;; This one is pretty similar to the -mem version of websocket-relay
(defn process-message
  "..."
  [msg store & {:keys [nrepl-handler read-timeout]
                :or   {nrepl-handler (nrepl-server/default-handler)
                       read-timeout  Long/MAX_VALUE}}]
  ;; TODO heartbeat for continuous feeding mode
  (let [[read write] (or (::transport store) ;;  :as transport
                         (do (.put store ::transport (transport/piped-transports))
                             (::transport store)))
        client (nrepl/client read read-timeout)
        reply-fn (partial process-replies
                          (fn [msg]
                            (json/write-str msg)))]

    (debug "Processing message " (with-out-str (pp/pprint msg) " response timeout = " read-timeout))
    (reply-fn
     (when (:op msg)
       (future (nrepl-server/handle* msg nrepl-handler write)))
     (client))))

;; Called from java
(defn process-json-message
  [data store]
  (let [_ (info "jee process message: " data)
        m (assoc (-> (json/read-str data) w/keywordize-keys) :as-html 1)]
    (-> m
        (process-message store :nrepl-handler @handler))))
