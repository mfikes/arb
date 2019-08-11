(ns arb.txt
  (:require
    [planck.core :refer [with-open]]
    [planck.io :as io]))

(defn spit
  ([file values]
   (spit file (count values) (apply max values) (apply min values) values))
  ([file count-values max-value min-value values]
   (with-open [w (io/writer file)]
     (let [writeln (fn [x]
                     (-write w (if (number? x)
                                 (.toFixed x 6)
                                 x))
                     (-write w "\n"))]
       (writeln (str count-values))
       (writeln max-value)
       (writeln min-value)
       (run! writeln values)
       (writeln "")))))
