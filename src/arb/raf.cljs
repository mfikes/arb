(ns arb.raf
  (:require
    [planck.core :refer [with-open -write-bytes]]
    [planck.io :as io]))

;; Illustrating making a change

(defn point->bytes [point]
  [(bit-and point 0xFF) (bit-or (bit-shift-right point 8) 0x80)])

(defn points->bytes [points]
  (into [] (mapcat point->bytes) points))

(defn value->point [low high value]
  (int (* (dec (Math/pow 2 14))
         (/ (- value low)
           (- high low)))))

(defn values->points [low high values]
  (map (partial value->point low high) values))

(defn spit
  ([file values]
   (let [low (apply min values)
         high (apply max values)]
     (spit file low high values)))
  ([file low high values]
   (with-open [os (io/output-stream file)]
     (-write-bytes os
       (points->bytes (values->points low high values))))))

;; The DG1022z base model can read in 2e6 samples, and can play them at 60 MSa/s

(comment
  (arb.raf/spit "/tmp/test.raf" -1.5 2.5 [-1.5 -0.5 -1.5 0.5 -1.5 -1.5 -1.5 -1.5])

  ;; Sin wave modulated with an up ramp at 1/10th base frequency
  (arb.raf/spit "/tmp/test.raf" -1.0 1.0 (map #(* (/ % 2e6) (Math/sin (* % 2 Math/PI (/ 1 2e5)))) (range 2e6)))

  ;; Alternating Square / Sin
  (arb.raf/spit "/tmp/sqsin.raf" (map (fn [idx] (if (even? (quot idx 1000))
                                                   (if (even? (quot idx 100))
                                                     1
                                                     -1)
                                                   (Math/sin (* idx 2 Math/PI (/ 1 2e2)))))
                                    (range 2e6)))
  )
