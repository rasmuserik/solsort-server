(ns solsort.apps.uccorg-monitor
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.sys.platform :refer [exec]]
    [solsort.sys.mbox :refer [route log]]
    [solsort.sys.util :refer [parse-json-or-nil]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(defn dev-proxy []
  (go
    (while true
      (do
        (log 'uccorg "(re-)starting dev proxy")
        (<! (exec "ssh uccorganism@93.165.158.107 -L 0.0.0.0:8080:localhost:8080 -N & SSH_PID=$!; sleep 300; kill $SSH_PID"))
        ;(<! (timeout 60000))
        ))))

(def status (atom nil))
(route "uccorg-status" (fn []
                         (log 'uccorg 'status @status)
                         @status))
(defn start []
  (log 'uccorg "starting uccorg monitor")
  (go
    (dev-proxy)
    (while true
      (reset! status (parse-json-or-nil (<! (exec "ssh uccorganism@93.165.158.107 'curl -s localhost:8080/status'"))))
      ; TODO include status again when less verbose (log 'uccorg 'ok status)
      (when-not @status
        (print 'uccorg "uccorg restart service")
        (print (js/Date.))
        (print (<! (exec  "ssh uccorganism@93.165.158.107 'killall VBoxHeadless; launchctl load Library/LaunchAgents/apiserver.plist; launchctl start apiserver'")))
        (<! (timeout 60000))
        (print "uccorg status:")
        (print (js/Date.))
        (print (<! (exec "ssh uccorganism@93.165.158.107 'curl -s localhost:8080/status'")))))
    (<! (timeout 60000))))
(route "uccorg-monitor" start)
