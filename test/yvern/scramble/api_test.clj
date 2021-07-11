(ns yvern.scramble.api-test
  (:require [clojure.test :as t]
            [org.httpkit.client :refer [post]]
            [org.httpkit.server :refer [server-status server-port server-stop!]]
            [yvern.scramble.api :refer [scramble-handler start]]))

(t/deftest api
  (t/testing "basic correct request yields status 200"
    (t/are [input] (-> {:body-params input} scramble-handler :status (= 200))
      {:letters "rekqodlw" :word "world"}
      {:letters "cedewaraaossoqqyt" :word "codewars"}
      {:letters "katas" :word "steak"}))

  (t/testing "requests with missing fields yield 400 and error message"
    (t/are [input err-msg]
           (-> {:body-params input} scramble-handler
               (= {:status 400 :body {:errors err-msg}}))
      {:letters "rekqodlw"}
      {:word ["missing required key"]}

      {:word "codewars"}
      {:letters ["missing required key"]}

      {:letters 99 :word true}
      {:letters ["should be a string"] :word ["should be a string"]}))

  (t/testing "server starts up corectly, handles requests and exits"
    (let [port 5678
          payload {:letters "abc" :word "c"}
          svr (start port)
          api (str "http://localhost:" port "/api")]

      (t/is (= :running (server-status svr)))
      (t/is (= port (server-port svr)))

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

      (t/is (true? @(server-stop! svr)))
      (t/is (= :stopped (server-status svr))))))
