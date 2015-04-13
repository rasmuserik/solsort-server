(ns solsort.log
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.sys.platform :as platform :refer [ensure-dir fs is-nodejs is-browser exec]]
    [solsort.sys.mbox :refer [log handle]]
    [clojure.string :as string]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(defn two-digits [n] (.slice (str (+ (mod n 100) 300)) 1))
(defn three-digits [n] (.slice (str (+ (mod n 1000) 3000)) 1))
(defn six-digits [n] (.slice (str (+ (mod n 1000000) 3000000)) 1))
(defn date-string []
  (let [now (js/Date.)]
    (string/join "" (map two-digits [(.getUTCFullYear now) (inc (.getUTCMonth now)) (.getUTCDate now)]))))
(defn time-string []
  (let [now (js/Date.)]
    (string/join "" (map two-digits [(.getUTCHours now) (.getUTCMinutes now) (.getUTCSeconds now)]))))
(defn timestamp-string []
  (str (date-string) "-" (time-string) "." (three-digits (.now js/Date))))
(def logfile-name (atom nil))
(def logfile-stream (atom nil))
(handle 
  "log"
  (fn [o]
    (let [msg (str (six-digits (aget (aget o "info") "src")) " "
                   (timestamp-string) " " 
                   (aget o "data"))]
      (if is-nodejs
        (let [date (date-string)
              logpath "logs/"
              logname (str logpath (.hostname (js/require "os")) "-" date ".log")]
          (if (not (= @logfile-name logname))
            (do
              (if @logfile-stream
                (let [oldname @logfile-name]
                  (.on @logfile-stream "close" (exec (str "xz -9 " oldname)))
                  (.end @logfile-stream)))
              (ensure-dir logpath)
              (reset! logfile-stream (.createWriteStream fs logname #js{:flags "a"}))
              (reset! logfile-name logname)))
          (.write @logfile-stream (str msg "\n"))))
      (.log js/console msg))))

(log 'system 'boot (str (if is-nodejs "node") (if is-browser "browser")))