(ns yvern.scramble.api
  (:require [yvern.scramble.core :refer [scramble?]]
            [yvern.scramble.pages :refer [index app]]
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

(defn scramble-handler [body]
  (if (valid? body)
    {:status 200
     :body (assoc body :scramble? (scramble? body))}
    {:status 400
     :body {:errors (me/humanize (explanation body))}}))

(defn router [{:keys [:request-method :uri] :as req}]
  (case [request-method uri]
    [:get "/"] {:body (index (app)) :status 200}
    [:post "/api"] ((wrap-format (comp (memoize scramble-handler) :body-params)) req)
    {:body "Not Found" :status 404}))

(defn start [port]
  (run-server #'router
              {:port port :legacy-return-value? false}))

(defn -main [& args]
  (let [port' (or (some-> args first Integer/parseInt) 8080)
        svr (start port')]
    (println "server" (server-status svr) "on:" (str "http://localhost:" (server-port svr)))
    (read-line)
    @(server-stop! svr)
    (println "server" (server-status svr))))

(comment
  (def svr (start 8080))
  @(server-stop! svr))