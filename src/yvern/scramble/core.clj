(ns yvern.scramble.core
  "namespace that holds the core logic for checking if with a pool of letters a given word can be formed."
  (:gen-class))

(defn char-left?
  "given a char histogram (as from frequencies on a string) and a single char,
   checks if the histogram contains it, and if the count is at least 1."
  [histogram letter]
  (and (contains? histogram letter)
       (pos? (histogram letter))))

(defn scramble-reducer
  "given a char histogram (as from frequencies on a string) and a single char,
   checks if the char is present and its count must be decremented, else ends reduction with false."
  [histogram letter]
  (if (char-left? histogram letter)
    (update histogram letter dec)
    (reduced false)))

(defn scramble-iter
  "given two strings, uses the first as scramble letters and the second as target word.
   iterates over target word, checking if the current char is available from the pool of chars left.
   by the end returns if the word can be formed from given letters."
  [letters word]
  (->> word vec
       (reduce scramble-reducer (frequencies letters))
       map?))

(defn scramble?
  "given two strings, uses the first as scramble letters and the second as target word.
   first checks for cases where the answer is trivial, then applies scramble if needed."
  ([{:keys [letters word]}] (scramble? letters word))
  ([letters word]
   (cond
     (= letters word) true
     (<= (count letters) (count word)) false
     :else (scramble-iter letters word))))

(defn -main
  "the entrypoint to check scrambability from the command line."
  [& args]
  (println
   (cond
     (not= 2 (count args)) "Please provide exactly 2 words to scramble"
     (apply scramble? args) "Yes we got scramble!"
     :else "No, words dont scramble ):")))
