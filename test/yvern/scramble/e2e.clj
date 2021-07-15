(ns yvern.scramble.e2e
  (:require [etaoin.api :as e]
            [clojure.test :as t]
            [yvern.scramble.api :refer [start stop port]]
            [clojure.java.io :as io]
            [ffclj.core :refer [ffmpeg!]]
            [clojure.string :as string]))

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
  (let [screenshots (io/file "screenshots")]
    (if (.exists screenshots)
      (->> screenshots file-seq (filter #(.isFile %)) (mapv io/delete-file))
      (.mkdir screenshots))
    (swap! driver make-driver)
    (f)
    (e/quit @driver)
    (->> screenshots file-seq (filter #(string/ends-with? % ".png")) (mapv io/delete-file))))

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
    (t/are [letters words result] (check-play letters words result)
      "rekqodlw" "world" true
      "cedewaraaossoqqyt" "codewars" true
      "katas"  "steak" false))

  (t/testing "caps at 10 latest reponses"
    (dotimes [_ 12] (check-play "world" "word" true))
    (t/is (>= 10 (count (e/query-all @driver {:tag :span :fn/text "Yeah! We got bramble!"}))))))

(t/deftest e2e-screenshots
  (t/testing "sample flow to generate screenshots"
    (e/with-wait 1
      (doto @driver
        (e/screenshot "screenshots/ss1.png")
        (e/fill {:tag :input :name :letters} "hello")
        (e/screenshot "screenshots/ss2.png")
        (e/fill {:tag :input :name :word} "world!")
        (e/screenshot "screenshots/ss3.png")
        (e/refresh)
        (e/fill {:tag :input :name :letters} "hello")
        (e/fill {:tag :input :name :word} "world")
        (e/screenshot "screenshots/ss4.png")
        (e/click-single {:tag :button})
        (e/screenshot "screenshots/ss5.png")
        (e/fill {:tag :input :name :letters} "world")
        (e/screenshot "screenshots/ss6.png")
        (e/fill {:tag :input :name :word} "word")
        (e/screenshot "screenshots/ss7.png")
        (e/click-single {:tag :button})
        (e/screenshot "screenshots/ss8.png")
        (e/has-text? "Yeah! We got bramble!")
        (e/has-text? "Words don't bramble...")))

    (with-open [task (ffmpeg!
                      [:y
                       :pattern_type "glob"
                       :i "screenshots/ss*.png"
                       :vf "palettegen"
                       "screenshots/palette.png"])]

      (.wait-for task)
      (t/is (= 0 (.exit-code task))))

    (with-open [task (ffmpeg!
                      [:y
                       :framerate 1.5
                       :pattern_type "glob"
                       :i "screenshots/ss*.png"
                       :i "screenshots/palette.png"
                       :filter_complex "paletteuse"
                       "screenshots/res.gif"])]

      (.wait-for task)
      (t/is (= 0 (.exit-code task))))))