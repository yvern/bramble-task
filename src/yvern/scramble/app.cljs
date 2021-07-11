(ns yvern.scramble.app
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [ajax.core :refer [POST]]))

(defn scramble!
  [plays play]
  (POST "/api" {:headers {"Accept" "application/transit+json"}
                :params play
                :handler #(swap! plays conj %)
                :error-handler prn}))

(def result-header :h2.title.has-text-centered.is-size-4)
(def center-p :p.is-size-4.has-text-centered)

(defn columned [s1 s1' s2]
  [:div.columns
   [:div.column.is-3]
   [:div.column.is-2 s1]
   [:div.column.is-2 s1']
   [:div.column.is-2 s2]
   [:div.column.is-3]])

(def section-hero
  [:section.hero.is-primary
   [:div.hero-body
    [:p.title "Welcome to Bramble!"]
    [:p.subtitle "A Scramble checker"]]])

(defn play-1 [{:keys [letters word scramble?]}]
  (columned [center-p letters]
            [center-p word]
            (if scramble?
              [:span.is-light.tag.is-success.is-large "Yeah! We got bramble!"]
              [:span.is-light.tag.is-danger.is-large "Words don't bramble..."])))

(defn text-in [play data]
  [:input.input.is-primary.is-size-5
   {:type "text" :placeholder (str "type here your " (name data))
    :on-change #(swap! play assoc data (-> % .-target .-value))
    :value (data @play)}])

(defn submit [play plays]
  [:button.button.is-link.is-fullwidth.is-size-5
   {:on-click #(do (scramble! plays @play)
                   (reset! play {}))}
   "Let's go!"])

(defn main []
  (let [plays (r/atom '())
        play (r/atom {})]
    (fn []
      (when (< 10 (count @plays)) (swap! plays take 10))
      [:div section-hero
       [:br]
       (columned (text-in play :letters)
                 (text-in play :word)
                 (submit play plays))
       [:br]
       (columned [result-header "Letters"]
                 [result-header "Word"]
                 [result-header "Bramble?"])
       (for [play @plays] [play-1 play])])))

(dom/render [main] (.getElementById js/document "content"))
