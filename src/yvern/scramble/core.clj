(ns yvern.scramble.core
  (:gen-class))

(defn char-left?
  "given a char histogram (as from frequencies on a string) and a single char,
   checks if the histogram contains it, and if the count is at least 1."
  [histogram char']
  (and (contains? histogram char')
       (pos? (histogram char'))))

(defn scramble-reducer
  "given a char histogram (as from frequencies on a string) and a single char,
   checks if the char is present and its count must be decremented, else ends reduction with false."
  [histogram char']
  (if (char-left? histogram char')
    (update histogram char' dec)
    (reduced false)))

(defn scramble-iter
  "given two strings, uses the first as scramble letters and the second as target word.
   iterates over target word, checking if the current char is available from the pool of chars left.
   by the end returns if the word can be formed from given letters."
  [str1 str2]
  (->> str2 vec
       (reduce scramble-reducer (frequencies str1))
       map?))

(defn scramble?
  "given two strings, uses the first as scramble letters and the second as target word.
   first checks for cases where the answer is trivial, then applies scramble if needed."
  [str1 str2]
  (cond
    (= str1 str2) true
    (<= (count str1) (count str2)) false
    :else (scramble-iter str1 str2)))

(defn -main [& args]
  (println
   (cond
     (not= 2 (count args)) "Please provide exactly 2 words to scramble"
     (apply scramble? args) "Yes we got scramble!"
     :else "No, words dont scramble ):")))
