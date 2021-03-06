(ns pinkgorilla.nrepl.server.nrepl-server
  (:require
   [taoensso.timbre :as timbre :refer [infof warn errorf]]
   [nrepl.server]
   [pinkgorilla.nrepl.handler.nrepl-handler :refer [make-default-handler]]
   ;; side effects
   [nrepl.middleware.print]
  ;picasso
   [picasso.default-config]
   [pinkgorilla.notebook.repl]
   [picasso.datafy.file]
  ; nrepl-miiddleware
   [pinkgorilla.nrepl.middleware.picasso]
   ;[pinkgorilla.nrepl.middleware.sniffer]
   ))

(defn run-nrepl-server [config]
  (let [config (merge {:enabled false
                       :bind "0.0.0.0"
                       :port 9100}
                      (or config  {}))
        {:keys [enabled bind port]} config]
    (if enabled
      (do
        (errorf "nrepl starting on %s:%s" bind port)
        (nrepl.server/start-server :bind bind
                                   :port port
                                   :handler (make-default-handler)))
      (do (warn "nrepl is disabled.")
          nil))))

