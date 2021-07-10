(ns yvern.scramble.core-test
  (:require [clojure.test :as t]
            [yvern.scramble.core :refer [scramble? char-left? scramble-reducer]]))

(t/deftest internal-functions
  (t/testing "if there are counts of the given char left"
    (t/are [histogram char' result]
           (= result (char-left?  histogram char'))
      {\a 1 \b 1 \c 1} \b true
      {\h 1 \e 1 \l 2 \o 1} \l true
      {\b 1 \i 1 \m 1 \o 1} \m true
      {\a 1 \b 1 \c 1} \d false
      {\h 1 \e 1 \l 2 \o 1} \k false
      {\b 1 \i 1 \m 1 \o 0} \o false))

  (t/testing "exits iteration early by wrapping in a `reduced`"
    (t/are [histogram char']
           ((every-pred reduced? (comp false? deref))
            (scramble-reducer histogram char'))
      {\a 1 \b 1 \c 1} \d
      {\h 1 \e 1 \l 2 \o 1} \k
      {\b 1 \i 1 \m 1 \o 0} \o))

  (t/testing "does not interfere with other char counts/keys"
    (t/are [histogram char']
           (= (dissoc histogram char') (dissoc (scramble-reducer histogram char') char'))
      {\a 1 \b 1 \c 1} \b
      {\h 1 \e 1 \l 2 \o 1} \l
      {\b 1 \i 1 \m 1 \o 1} \m)))

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
      "qwe" "tyuip"))

  (t/testing "given specification"
    (t/are [str1 str2 result] (= result (scramble? str1 str2))
      "rekqodlw" "world" true
      "cedewaraaossoqqyt" "codewars" true
      "katas" "steak" false)))
