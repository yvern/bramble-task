(ns yvern.scramble.api-test
  (:require [clojure.test :as t]
            [org.httpkit.client :refer [post]]
            [yvern.scramble.api :refer [scramble-handler start port stop status]]))

(t/deftest api
  (t/testing "basic correct request yields status 200"
    (t/are [input] (-> input scramble-handler :status (= 200))
      {:letters "rekqodlw" :word "world"}
      {:letters "cedewaraaossoqqyt" :word "codewars"}
      {:letters "katas" :word "steak"}))

  (t/testing "requests with missing fields yield 400 and error message"
    (t/are [input err-msg]
           (= {:status 400 :body (assoc input :errors err-msg)}
              (scramble-handler input))
      {:letters "rekqodlw"}
      {:word ["missing required key"]}

      {:word "codewars"}
      {:letters ["missing required key"]}

      {:letters "99" :word "!true"}
      {:letters ["should match regex"]
       :word ["should match regex"]}

      {:letters 99 :word true}
      {:letters ["should be a string" "should match regex"]
       :word ["should be a string" "should match regex"]}))

  (t/testing "server starts up corectly, handles requests and exits"
    (let [port' 5678
          payload {:letters "abc" :word "c"}
          svr (start port')
          api (str "http://localhost:" port' "/api")]

      (t/is (= :running (status svr)))
      (t/is (= port' (port svr)))

      (let [{:keys [status body headers]}
            @(post api {:headers {"content-type" "application/edn"
                                  "accept" "application/edn"}
                        :body (str payload)})]

        (t/is (= 200 status))
        (t/is (->> headers :content-type (= "application/edn; charset=utf-8")))
        (t/is (= (-> payload (assoc :scramble? true) str)
                 (slurp body))))

      (let [{:keys [status body headers]}
            @(post api {:headers {"content-type" "application/json"
                                  "accept" "application/json"}
                        :body "{}"})]

        (t/is (= 400 status))
        (t/is (->> headers :content-type (= "application/json; charset=utf-8")))
        (t/is (= "{\"errors\":{\"letters\":[\"missing required key\"],\"word\":[\"missing required key\"]}}"
                 body)))

      (t/is (true? @(stop svr)))
      (t/is (= :stopped (status svr))))))
