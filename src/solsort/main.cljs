(ns solsort.main
  (:require
    [solsort.uccorg-monitor]
    [solsort.bib-related]
    [solsort.bib-process]
    ))

(enable-console-print!)

(def commands (atom {}))
(defn register [cmd f] (swap! commands assoc cmd f))

(register "uccorg-monitor" solsort.uccorg-monitor.start)
(register "bib-related" solsort.bib-related.start)
(register "bib-process" solsort.bib-process.start)

(def arg
  (or
    (and js/window.process (get js/window.process.argv 2))
    (and (exists? js/window) js/window js/window.location (.slice js/window.location.hash 1))))

((or (get @commands arg)
     (fn []
       (print "possible arguments:")
       (doall (map (fn [[a b]] (print a)) @commands)))))

