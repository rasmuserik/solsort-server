(ns solsort.sys.mbox
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cognitect.transit :as transit]
    [clojure.string :as string]
    [cljs.core.async.impl.channels :refer [ManyToManyChannel]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(declare log)
(declare -route-error)
(declare route-error-fn)
(declare post)
(declare local)
(declare processes)


;; private utility functions
(def -writer (transit/writer :json))
(def -reader (transit/reader :json))
(defn -route-error [msg]
  (js/console.log "route-error" (js/JSON.stringify msg))
  (let [info (aget msg "info")
        rbox (aget info "rbox")]
    (if rbox
      (post (aget info "rpid") rbox nil))))
(def -mboxes "mboxes for local process" (atom {}))
(def -unique-id-counter (atom 0))
(defn -unique-id [] (str "id" (swap! -unique-id-counter inc)))
(defn -local-handler [msg] 
  ((get @-mboxes (aget msg "mbox") @route-error-fn) msg))


(defn transit-read [o] (transit/read -reader o))
(defn transit-write [o] (transit/write -writer o))

;; internal message passing / low level api
(def route-error-fn (atom -route-error))
(defn msg "construct a message object"
  ([pid mbox data] (msg pid mbox data #js{:src local}))
  ([pid mbox data info] 
   #js{:info info :data data :mbox mbox :pid pid}))
(defn post "send a message to a mbox"
  ([msg] (let [pid (aget msg "pid")
               handler (if (= pid local)
                         -local-handler
                         (get @processes pid @route-error-fn))]
           (handler msg)))
  ([pid mbox data] (post (msg pid mbox data)))
  ([pid mbox data info] (post (msg pid mbox data info))))
(defn handle "register a local handler for messages"
  [mbox handler] (swap! -mboxes assoc mbox handler))
(defn unhandle [mbox] (swap! -mboxes dissoc mbox))
(defn local-mbox? [mbox] (contains? @-mboxes mbox))
(defn local-mboxes [] (keys @-mboxes))


;; processes / pid-list
(def local (if (exists? js/process) js/process.pid (bit-or 0 (+ 65536 (* (js/Math.random) (- 1000000 65536))))))
(def parent (atom 0))
(def clients (atom #{}))
(def workers (atom #{}))
(def peers (atom #{}))
(def processes "mapping from from reachable pids to function that receive messages" 
  (atom {local -local-handler}))


;; high level api
(defn call-timeout [max-wait pid mbox & args] 
  (let [c (chan)
        rbox (-unique-id)
        handler 
        (fn [msg]
          (unhandle rbox)
          (let [data (transit-read (aget msg "data"))]
            (if (nil? data)
              (close! c)
              (put! c data))))]
    (handle rbox handler)
    (post pid mbox (transit-write args) #js{:rpid local :rbox rbox :src local})
    (if max-wait (go (<! (timeout max-wait)) (handler #js{})))
    c))
(defn call [pid mbox & args] (apply call-timeout false pid mbox args))
(defn route [mbox f]
  (handle 
    mbox 
    (fn [msg]
      (go
        (let [result (apply f (or (transit-read (aget msg "data")) []))
              result (if (instance? ManyToManyChannel result) (<! result) result)
              info (aget msg "info")]
          (post (aget info "rpid") (aget info "rbox") (transit-write result)))))))


;; Logging
(defn log [& args] (post local "log" (string/join " " (map pr-str args))))
(defn warn [& args] (apply log 'warn args))