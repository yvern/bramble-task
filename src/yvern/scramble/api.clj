(ns yvern.scramble.api
  "defines the http server and the api that uses the core scramble logic.
    also hosts the webapp that calls the api."
  (:require [yvern.scramble.core :refer [scramble?]]
            [yvern.scramble.pages :refer [index app]]
            [muuntaja.middleware :refer [wrap-format]]
            [org.httpkit.server :refer [run-server server-status server-port server-stop!]]
            [malli.core :as m]
            [malli.error :as me])
  (:gen-class))

(def stop server-stop!)
(def port server-port)
(def status server-status)

(def scramble-play
  "malli schema for a valid scramble play."
  [:map {:registry {::valid-text [:and :string [:re "^[a-z]+$"]]}}
     [:letters ::valid-text]
     [:word ::valid-text]])

(def valid? (m/validator scramble-play))

(def explanation (m/explainer scramble-play))

(defn scramble-handler
  "body handler that given a valid scramble play, checks its result.
   on invalid one returns an explanation as from malli."
  [body]
  (if (valid? body)
    {:status 200
     :body (assoc body :scramble? (scramble? body))}
    {:status 400
     :body  (assoc body :errors (me/humanize (explanation body)))}))

(defn router
  "basic router that handles the 2 known endpoints, hosting the spa and the api."
  [{:keys [:request-method :uri] :as req}]
  (case [request-method uri]
    [:get "/"] {:body (index (app)) :status 200}
    [:post "/api"] ((wrap-format (comp (memoize scramble-handler) :body-params)) req)
    {:body "Not Found" :status 404}))

(defn start
  "starts the server on given port, and returns the server object, that can be checked on or stopped."
  [port']
  (run-server #'router {:port port' :legacy-return-value? false}))

(defn -main
  "entrypoint for the server application, that runs on port given as first and only argument, or 8080 by default.
   blocks, but can be gracefully halted by entering a newline (enter) on the terminal its running."
  [& args]
  (let [port' (or (some-> args first Integer/parseInt) 8080)
        svr (start port')]
    (println "server" (status svr) "on:" (str "http://localhost:" (port svr)))
    (read-line)
    @(stop svr)
    (println "server" (status svr))))

(comment
  (def svr (start 8080))
  (clojure.java.browse/browse-url (str "http://localhost:" (port svr)))
  @(stop svr))