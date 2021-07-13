(ns yvern.scramble.pages
  "template for the index page of the webapp,
   as well as loading the clojurescript source at either execution or compile time."
  (:require [hiccup.core :refer [html]]
            [clojure.string :as string]))

(defn load-app [] (slurp "src/yvern/scramble/app.cljs"))

(defmacro app'
  "macro that checks whether the project is being compiled, and loads the cljs source in memory,
   or if executing from a repl and loads from filesystem, enabling reloading and development."
  []
  (if *compile-files*
    `(let [app# ~(string/replace (load-app) #"\s+" " ")] (fn [] app#))
    load-app))

(def app (app'))

(defn index'
  "hiccup template for index.html, receiving the cljs source code
   for the webapp as a parameter to be injected on the correct tag."
  [app-src]
  (html
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:link {:rel "shortcut icon" :href "data:,"}]
     [:link {:rel "apple-touch-icon" :href "data:,"}]
     [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css"}]
     [:script {:crossorigin nil :src "https://unpkg.com/react@17/umd/react.production.min.js"}]
     [:script {:crossorigin nil :src "https://unpkg.com/react-dom@17/umd/react-dom.production.min.js"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.2/js/scittle.js" :type "application/javascript"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.2/js/scittle.reagent.js" :type "application/javascript"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.2/js/scittle.cljs-ajax.js" :type "application/javascript"}]
     [:title "Bramble"]]
    [:body
     [:div {:id "content"}]
     [:script {:type "application/x-scittle"} app-src]]]))

(def index (memoize index'))