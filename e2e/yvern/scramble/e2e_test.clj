(ns yvern.scramble.e2e-test
  (:require [etaoin.api :as e]
            [clojure.test :as t]
            [yvern.scramble.api :refer [start stop port]]))

(def svr (atom 8080))
(def driver (atom nil))

(defn with-server [f]
  (swap! svr start)
  (f)
  @(stop @svr))

(defn with-driver [f]
  (reset! driver (e/chrome))
  (f)
  (e/quit @driver))

(defn refresh [f]
  (e/go @driver (str "http://localhost:" (port @svr)))
  (f)
  (e/refresh @driver))

(t/use-fixtures :once with-server with-driver)
(t/use-fixtures :each refresh)

(defn fill-play [letters word]
  (doto @driver
    (e/fill {:tag :input :name :letters} letters)
    (e/fill {:tag :input :name :word} word)))

(defn check-play [letters word result]
  (fill-play letters word)
  (e/click-single @driver {:tag :button})
  (e/wait-has-text @driver {:tag :span} "bramble")
  (e/has-text? @driver (if result "Yeah! We got bramble!" "Words don't bramble...")))

(t/deftest e2e-test-static
  (t/testing "should show these"
    (t/are [txt] (e/has-text? @driver txt)
      "Welcome"
      "Bramble!"
      "Scramble"
      "Let's go!"
      "Letters"
      "Words"
      "Bramble?")))

(t/deftest e2e-interactions
  (t/testing "input and response from spec"
    (t/are [letters words result]
           (check-play letters words result)
      "rekqodlw" "world" true
      "cedewaraaossoqqyt" "codewars" true
      "katas"  "steak" false)))