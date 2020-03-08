(ns pinkgorilla.middleware.cljs
  "Experimental CLJS - not quite sure if that makes sense at all"
  (:require
   [pinkgorilla.middleware.handle :refer [nrepl-handler]]
   [pinkgorilla.middleware.sandboxed_interruptible-eval]
   [pinkgorilla.middleware.render-values :as render-mw] ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
   ;; [clojure.tools.nrepl.server :as server]
   ;; [cider.piggieback :as pb]
   ))

(def ^:private cljs-middleware
  "A vector containing the cljs gorilla repl supports."
  '[cider.piggieback/wrap-cljs-repl])

;; TODO middleware must resolve
(defn cljs-handler []
  (require 'cider.piggieback)
  (nrepl-handler false cljs-middleware))
