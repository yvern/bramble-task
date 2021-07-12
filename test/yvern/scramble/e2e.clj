(ns yvern.scramble.e2e
  (:require [etaoin.api :as e]
            [clojure.test :as t]
            [yvern.scramble.api :refer [start stop port]]))

(defn get-driver
  "utility to try out getting a driver from possible installed"
  ([] [])
  ([x] (if-not (vector? x) x
               (throw (ex-info "Failed to get a driver"
                               {:tries x}))))
  ([fails driver']
   (try (reduced (driver'))
        (catch Exception e
          (conj fails (ex-message e))))))

(defn make-driver
  "iterates over etaoin supported drivers to find one to use"
  [_]
  (transduce identity
             get-driver
             [e/firefox-headless
              e/chrome-headless
              e/phantom
              e/safari
              e/edge-headless]))

(def svr (atom 8080))
(def driver (atom nil))

(defn with-server [f]
  (swap! svr start)
  (f)
  @(stop @svr)
  (swap! svr port))

(defn with-driver [f]
  (swap! driver make-driver)
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
      "Bramble?"))

  (t/testing "submit button should be disabled on invalid inputs"
    (t/is (e/query @driver {:tag :button :fn/disabled true}))
    (t/are [letters word] (do
                            (e/refresh @driver)
                            (fill-play letters word)
                            (e/query @driver {:tag :button :fn/disabled true}))
           "" ""
           "kushfh" ""
           "" "lisrhjg"
           "vsku4634" "klh"
           "kdjfg" "dfgh =")))

(t/deftest e2e-interactions
  (t/testing "input and response from spec"
    (t/are [letters words result]
           (check-play letters words result)
      "rekqodlw" "world" true
      "cedewaraaossoqqyt" "codewars" true
      "katas"  "steak" false)))