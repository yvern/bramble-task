(ns yvern.scramble.pages
  (:require [hiccup.core :refer [html]]))

(defn load-app []
  (prn "loading app.cljs")
  (slurp "src/yvern/scramble/app.cljs"))

(defmacro app' []
    (if *compile-files*
      `(let [app# ~(load-app)] (fn [] app#))
      load-app))

(def app (app'))

(defn index' [app-src]
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