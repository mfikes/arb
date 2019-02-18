(ns arb.raf
  (:require
    [planck.core :refer [with-open -write-bytes]]
    [planck.io :as io]))

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

(defn number->bytes [bits n]
  (mapv #(bit-and (bit-shift-right n %) 0xFF) (range 0 bits 8)))

(defn output-mode->byte [output-mode]
  (case output-mode
    :period 0
    :sample-rate 1))

(defn filename->bytes [filename]
  (let [unpadded (mapv #(.charCodeAt % 0) (take 25 filename))]
    (into unpadded (repeat (- 25 (count unpadded)) 0))))

(defn calculate-header [file rate low high values]
  (-> []
    (into (number->bytes 32 (count values)))
    (into [1 0])
    (into [(output-mode->byte :sample-rate)])
    (into (filename->bytes "8.RAF"))
    (into (number->bytes 32 rate))                          ; low-bytes of rate
    (into [0 0 0 0])                                        ; high bytes of rate
    (into (number->bytes 32 (* high 1e7)))                  ; high V
    (into (number->bytes 32 (* low 1e7)))                   ; low V
    (into (number->bytes 16 0xd650))                        ; data CRC
    (into (number->bytes 16 0x1097))                        ; header CRC
    (into [0 0 0 0])))

(defn spit [file rate low high values]
  (prn (points->bytes (values->points low high values)))
  (with-open [os (io/output-stream file)]
    (-write-bytes os
      (into (calculate-header file rate low high values)
        (points->bytes (values->points low high values))))))

(comment
  (spit "/Volumes/RIGOL/test.raf" 10000000 -1.5 2.5 [-1.5 -0.5 -1.5 0.5 -1.5 -1.5 -1.5 -1.5]))
