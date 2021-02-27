(ns pinkgorilla.nrepl.client.op.eval
  (:require
   #?(:cljs [taoensso.timbre :refer-macros [debug info warn error]]
      :clj [taoensso.timbre :refer [debug info warn error]])
   #?(:cljs [cljs.reader :refer [read-string]]
      :clj [clojure.core :refer [read-string]])
   #?(:cljs [js :refer [Error]]
      ;:clj [java.lang :refer [Error]]
            )
   [pinkgorilla.nrepl.client.protocols :refer [init]]))

;#?(:clj
;   (def Error {}))

(defn- picasso-unwrap
  "picasso middleware serializes picasso values to edn.
   It does this becaus nrepl might usesay bencode for its connection.
   In this case we would loose meta-data, Therefor picasso values ae sent as string
   on the wire.
   In case nrepl transport is edn (which new versions do), then
   we have edn within edn :-(.
   "
  [value]
  (try
    (let [data (read-string value)]
    ;(info "converted value-response" (:value-response data2))
      data)
    (catch Error e 
      (error "picasso-unwrap parsing %s ex: %s" value e))))



(defn process-fragment
  "result is an atom, containing the eval result.
   processes a fragment-response and modifies result-atom accordingly."
  [result {:keys [out err root-ex ns value picasso datafy status] :as message}]
  (-> result
      ; console 
      (cond-> out (assoc :out (str (:out result) out)))

      ; eval error
      (cond-> err (assoc :err err))

      ; datafy
      (cond-> datafy (assoc :datafy datafy))

      ; value /namespace
      (cond-> ns (assoc :ns ns
                        :value (conj (:value result) value)
                        :picasso (conj (:picasso result) (picasso-unwrap picasso))))

      ; root exception ?? what is this ?? where does it come from ? cider? nrepl?
      (cond-> root-ex (assoc :root-ex root-ex))))

(defmethod init :eval [req]
  (info "processing :op :eval")
  {:initial-value {:value []
                   :picasso []
                   :ns nil
                   :out ""
                   :err []
                   :root-ex nil}
   :process-fragment process-fragment})
