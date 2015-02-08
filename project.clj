(defproject solsort "0.1.0-SNAPSHOT"
  :description "solsort.com server"
  :url "http://solsort.com"

  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/clojurescript "0.0-2760"]
                 [figwheel "0.2.3-SNAPSHOT"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :plugins 
  [[lein-cljsbuild "1.0.4"]
   [lein-figwheel "0.2.3-SNAPSHOT"]]

  :source-paths ["src"]
  :figwheel { :http-server-root ""}

  :cljsbuild { 
    :builds [
             {:id "debug"
              :source-paths ["src" "debug"]
              :compiler {
                :output-to "resources/build/debug/debug.js"
                :output-dir "resources/build/debug"
                :optimizations :none
                :pretty-print true
                :source-map "resources/build/debug/debug.map"}}
             {:id "solsort"
              :source-paths ["src"]
              :compiler {
                :output-to "resources/build/solsort/solsort.js"
                :output-dir "resources/build/solsort/"
                :optimizations :advanced
                :externs ["externs/express.js"
                          "node_modules/nodejs-externs/externs/assert.js" "node_modules/nodejs-externs/externs/buffer.js" "node_modules/nodejs-externs/externs/child_process.js" "node_modules/nodejs-externs/externs/cluster.js" "node_modules/nodejs-externs/externs/core.js" "node_modules/nodejs-externs/externs/crypto.js" "node_modules/nodejs-externs/externs/dgram.js" "node_modules/nodejs-externs/externs/dns.js" "node_modules/nodejs-externs/externs/domain.js" "node_modules/nodejs-externs/externs/events.js" "node_modules/nodejs-externs/externs/fs.js" "node_modules/nodejs-externs/externs/http.js" "node_modules/nodejs-externs/externs/https.js" "node_modules/nodejs-externs/externs/net.js" "node_modules/nodejs-externs/externs/os.js" "node_modules/nodejs-externs/externs/path.js" "node_modules/nodejs-externs/externs/process.js" "node_modules/nodejs-externs/externs/punycode.js" "node_modules/nodejs-externs/externs/querystring.js" "node_modules/nodejs-externs/externs/readline.js" "node_modules/nodejs-externs/externs/repl.js" "node_modules/nodejs-externs/externs/stream.js" "node_modules/nodejs-externs/externs/string_decoder.js" "node_modules/nodejs-externs/externs/tls.js" "node_modules/nodejs-externs/externs/tty.js" "node_modules/nodejs-externs/externs/url.js" "node_modules/nodejs-externs/externs/util.js" "node_modules/nodejs-externs/externs/vm.js" "node_modules/nodejs-externs/externs/zlib.js"]
                :pretty-print true
                :source-map "resources/build/solsort/solsort.map"}}]})
