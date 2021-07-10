(ns yvern.scramble.core
  (:gen-class))

(defn char-left?
  [histogram char']
  (and (contains? histogram char')
       (pos? (histogram char'))))

(defn scramble-reducer
  [histogram char']
  (if (char-left? histogram char')
    (update histogram char' dec)
    (reduced false)))

(defn scramble-iter
  [str1 str2]
  (->> str2 vec
       (reduce scramble-reducer (frequencies str1))
       map?))

(defn scramble?
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