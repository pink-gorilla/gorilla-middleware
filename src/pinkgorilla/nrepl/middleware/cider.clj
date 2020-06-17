(ns pinkgorilla.nrepl.middleware.cider
  (:require
   [pinkgorilla.nrepl.middleware.handle :refer [nrepl-handler]]
   [pinkgorilla.nrepl.middleware.sandboxed_interruptible-eval]
   [pinkgorilla.nrepl.middleware.render-values :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround a weakness in nREPL's middleware resolution).
   ;; [cider.nrepl]
   ))

(def ^:private cider-middleware
  "A vector containing the CIDER middleware pinkgorilla supports."
  '[cider.nrepl/wrap-complete
    cider.nrepl/wrap-info
    cider.nrepl/wrap-stacktrace])

(defn cider-handler []
  ;; force side effects at runtime
  (require 'cider.nrepl)
  (nrepl-handler false cider-middleware))