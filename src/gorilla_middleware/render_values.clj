(ns gorilla-middleware.render-values
  (:require 
   [clojure.tools.nrepl.transport :as transport]
   [clojure.tools.nrepl.middleware.pr-values]
   [clojure.tools.nrepl.middleware :as middleware]
   [gorilla-middleware.json :as json]
   ;; [clojure.data.json :as json]
   #_[cheshire.core :as json]
   ;[gorilla-renderable.core :as render]
   [pinkgorilla.ui.gorilla-renderable :refer [render]]
   )
  #_(:refer [clojure.data.json :rename {write-str generate-string}])
  (:import clojure.tools.nrepl.transport.Transport))

;; There's absolutely no way I would have figured this out without referring to
;; https://github.com/clojure/tools.nrepl/blob/master/src/main/clojure/clojure/tools/nrepl/middleware/pr_values.clj
;; and as a result the structure of this follows that code rather closely (which is a fancy way of saying I copied it).

;; This middleware function calls the gorilla-repl render protocol on the value that results from the evaluation, and
;; then converts the result to json.
;; TODO: Would be awesome to make JSON serialization swapable
(defn render-values
  [handler]
  (fn [{:keys [op ^Transport transport] :as msg}]
    (handler (assoc msg :transport (reify Transport
                                     (recv [this] (.recv transport))
                                     (recv [this timeout] (.recv transport timeout))
                                     (send [this resp]
                                       (.send transport
                                              (if-let [[_ v] (and (:as-html msg) (find resp :value))]
                                                ;; we have to transform the rendered value to JSON here, as otherwise
                                                ;; it will be pr'ed by the pr-values middleware (which comes with the
                                                ;; eval middleware), meaning that it won't be mapped to JSON when the
                                                ;; whole message is mapped to JSON later. This has the unfortunate side
                                                ;; effect that the string will end up double-escaped.
                                                ;; (assoc resp :value (json/generate-string (render/render v)))
                                                ;; TODO: We actually want the serialization to be swappable
                                                (assoc resp :value (json/serialize (render v)))
                                                resp))
                                       this))))))


;; Unfortunately nREPL's interruptible-eval middleware has a fixed dependency on the pr-values middleware. So here,
;; what we do is fudge the :requires and :expects values to ensure that our rendering middleware gets inserted into
;; the linearized middlware stack between the eval middleware and the pr-values middleware. A bit of a hack!
(middleware/set-descriptor! #'render-values
                            {:requires #{#'clojure.tools.nrepl.middleware.pr-values/pr-values}
                             :expects  #{"eval"}
                             :handles  {}})
