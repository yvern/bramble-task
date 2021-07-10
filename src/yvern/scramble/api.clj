(ns yvern.scramble.api
  (:require [yvern.scramble.core :refer [scramble?]]
            [muuntaja.middleware :refer [wrap-format]]
            [org.httpkit.server :refer [run-server server-status server-port server-stop!]]
            [malli.core :as m]
            [malli.error :as me])
  (:gen-class))

(def scramble-play
  "schema for a valid scramble play."
  [:map
   [:letters string?]
   [:word string?]])

(def valid? (m/validator scramble-play))

(def explanation (m/explainer scramble-play))

(defn handler [{body :body-params}]
  (if (valid? body)
    {:status 200
     :body (assoc body :scramble? (scramble? body))}
    {:status 400
     :body {:errors (me/humanize (explanation body))}}))

(defn start [port]
  (run-server (wrap-format #'handler)
              {:port port :legacy-return-value? false}))

(defn -main [& args]
  (let [port (or (some-> args first Integer/parseInt) 8080)
        svr (start port)]
    (println "server" (server-status svr) "on port" (server-port svr))
    (read-line)
    @(server-stop! svr)
    (println "server" (server-status svr))))
