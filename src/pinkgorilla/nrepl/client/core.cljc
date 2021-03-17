(ns pinkgorilla.nrepl.client.core
  (:require
   #?(:clj [clojure.core.async :as async :refer [<! >! chan timeout close! go go-loop <!!]]
      :cljs [cljs.core.async :as async :refer [<! >! chan timeout close!] :refer-macros [go go-loop]])
   #?(:cljs [taoensso.timbre :refer-macros [debugf info infof]]
      :clj [taoensso.timbre :refer [debugf info infof]])

   ; side-effects (register multi-methods)
   [pinkgorilla.nrepl.client.op.eval]
   [pinkgorilla.nrepl.client.op.concat]
   [pinkgorilla.nrepl.client.op.cider]
   [pinkgorilla.nrepl.client.op.admin]
   [pinkgorilla.nrepl.client.op.gorilla]

   [pinkgorilla.nrepl.client.connection :refer [connect! disconnect!]]
   [pinkgorilla.nrepl.client.multiplexer :refer [create-multiplexer!]]
   [pinkgorilla.nrepl.client.request :as r]))

(defn connect [config]
  (let [conn (connect! config)
        mx (create-multiplexer! conn)]
    {:config config
     :conn conn
     :mx mx}))

(defn disconnect [s]
  (disconnect! (:conn s)))

(defn send-request! [{:keys [conn mx]} req & [partial-results?]]
  (let [result-ch (r/send-request! conn mx partial-results? req)]
    result-ch))

#_(defn send-requests!
    [s reqs]
    (go-loop [todo reqs]
      (when-let [req (first todo)]
        (send-request! s req false)
        (recur (rest todo)))))

#?(:clj

   (defn send-request-sync!
     " <!!  only works in clj. It is used to block until chan val arrives"
     [c req]
     (let [result-ch (send-request! c req false)
           r (<!! result-ch)]
       (infof "result: %s result-ch: " r result-ch)
       r))
 ;  
   )

(defn request-rolling!
  "make a nrepl request ´req´ and for each partial reply-fragment
   execute ´fun´"
  [c req fun]
  (if-let [result-ch (send-request! c req true)]
    (go-loop [result (<! result-ch)]
      (fun result)
      (recur (<! result-ch)))
    (info "cannot send nrepl msg. not connected!")))


; request op helper (is this really needed?)


(defn op-describe []
  {:op "describe"})

(defn op-lssessions []
  {:op "ls-sessions"})

(defn op-lsmiddleware []
  {:op "ls-middleware"})

(defn op-interrupt []
  {:op :interrupt})

;todo:
; op: close
; op: clone

(defn op-eval
  "evals code"
  [code]
  {:op "eval" :code code})

; cider ops
; https://docs.cider.mx/cider-nrepl/nrepl-api/ops.html

(defn op-ciderversion
  "cider version
   Relies on the cider-nrepl middleware."
  []
  {:op "cider-version"})

(defn op-docstring
  "Queries the REPL server for docs for the given symbol. 
   Relies on the cider-nrepl middleware."
  [symbol ns]
  {:op "complete-doc" :symbol symbol :ns ns})

(defn op-apropos
  "Relies on the cider-nrepl middleware."
  [query]
  {:op "apropos" :query query})

(defn op-completions
  "Query the REPL server for autocompletion suggestions. 
   Relies on the cider-nrepl middleware."
  [symbol ns context]
  {:op "complete" :symbol symbol :ns ns :context context})

(defn op-resolve-symbol
  "resolve a symbol to get its namespace takes the symbol and the namespace 
   that should be used as context.
   Relies on the cider-nrepl middleware. 
   Returns:
   - the symbol and the symbol's namespace"
  [symbol ns]
  {:op "info" :symbol symbol :ns ns})

(defn op-stacktrace
  "resolve a symbol to get its namespace takes the symbol and the namespace that should be used as context.
  Relies on the cider-nrepl middleware. Calls back with the symbol and the symbol's namespace"
  []
  {:op "stacktrace"})



; helper like this might be handy:


#_(defn ^:export nrepl-eval-cb
    "evaluates a clojure expression
   returns result via callback
   
   execute this in browser console:
   pinkgorilla.kernel.nrepl.clj_eval_cb (\"(+ 7 9 )\", 
    (function (r) {console.log (\"result!!: \" +r);}))
   "
    [conn snippet cb]
    (let [ch (nrepl-eval conn snippet)]
      (go
        (cb (<! ch)))))

#_(defn ^:export fn-eval
    "executes a clojure ```function-as-string``` (from clojurescript) "
    [conn function-as-string & params]
    (let [_ (info "params: " params)
          code (concat ["(" function-as-string] params [")"])]
      (nrepl-eval conn code)))

