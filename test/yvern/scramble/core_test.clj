(ns yvern.scramble.core-test
  (:require [clojure.test :as t]
            [yvern.scramble.core :refer [scramble? char-left? scramble-reducer scramble-iter]]))

(t/deftest inner-calls
  (t/testing "if there are counts of the given char left"
    (t/are [histogram char' result] (= result (char-left?  histogram char'))
      {\a 1, \b 1, \c 1} \b true
      {\h 1, \e 1, \l 2, \o 1} \l true
      {\b 1, \i 1, \m 1, \o 1} \m true
      {\a 1, \b 1, \c 1} \d false
      {\h 1, \e 1, \l 2, \o 1} \k false
      {\b 1, \i 1, \m 1, \o 0} \o false)))

(t/deftest scramble
  (t/testing "equal strings should be the scramble of one another"
    (t/are [str'] (scramble? str' str')
      "same"
      "string"
      "jknfvdbi"))

  (t/testing "when first string is shorter, should not be a scramble"
    (t/are [str1 str2] (not (scramble? str1 str2))
      "a" "abc"
      "shorter" "shorters"
      "qwe" "tyuip")))


;; (scramble? “rekqodlw” ”world”) ==> true
;; (scramble? “cedewaraaossoqqyt” ”codewars”) ==> true
;; (scramble? “katas”  “steak”) ==> false
