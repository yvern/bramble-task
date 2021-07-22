(ns yvern.scramble.app
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [ajax.core :refer [POST]]))

(defn invalid-play? [play]
  (->> play vals (not-every? #(re-matches #"^[a-z]+$" %))))

(defn scramble-handler [plays result]
  (swap! plays #(->> % (cons result) (take 10))))

(defn scramble!
  [plays play]
  (POST "/api" {:headers {"Accept" "application/transit+json"}
                :params play
                :handler (partial scramble-handler plays)
                :error-handler prn}))

(def result-header :h2.title.has-text-centered.is-size-4)
(def center-p :p.is-size-4.has-text-centered)

(defn columned [s1 s1' s2]
  [:div.columns
   [:div.column.is-2]
   [:div.column.is-3 s1]
   [:div.column.is-2 s1']
   [:div.column.is-2 s2]
   [:div.column.is-3]])

(def section-hero
  [:section.hero.is-primary
   [:div.hero-body
    [:p.title "Welcome to Bramble!"]
    [:p.subtitle "A Scramble checker"]]])

(defn play-1 [{:keys [letters word scramble?]}]
  [columned
   [center-p letters]
   [center-p word]
   (if scramble?
     [:span.is-light.tag.is-success.is-large "Yeah! We got bramble!"]
     [:span.is-light.tag.is-danger.is-large "Words don't bramble..."])])

(defn text-in [play data update-text]
  [:input.input.is-primary.is-size-5
   {:type "text"
    :placeholder (name data)
    :name data
    :on-change (partial update-text data)
    :value (data play)}])

(defn submit [play do-play]
  [:button.button.is-link.is-fullwidth.is-size-5
   {:on-click do-play
    :disabled ((memoize invalid-play?) play)}
   "Let's go!"])

(defn main []
  (let [plays (r/atom '())
        new-play (r/atom {:letters "" :word ""})
        update-text #(swap! new-play assoc %1 (-> %2 .-target .-value))
        do-play #(do (scramble! plays @new-play)
                     (doto new-play
                       (swap! assoc :letters "")
                       (swap! assoc :word "")))]
    (fn []
      [:div section-hero
       [:br]
       [columned
        [text-in @new-play :letters update-text]
        [text-in @new-play :word update-text]
        [submit @new-play do-play]]
       [:br]
       [columned
        [result-header "Letters"]
        [result-header "Word"]
        [result-header "Bramble?"]]
       (for [play @plays] [play-1 play])])))

(dom/render [main] (.getElementById js/document "content"))
