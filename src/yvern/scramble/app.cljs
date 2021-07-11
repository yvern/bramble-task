(ns yvern.scramble.app
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [ajax.core :refer [GET POST]]
            [clojure.string :as string]))

(defn scramble?
  [letters word]
  (POST "/api" {:headers {"Accept" "application/transit+json"}
                :params {:letters letters :word word}
                :handler prn
                :error-handler prn}))

(scramble? "yaaay" "yaty")