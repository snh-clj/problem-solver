(ns snh-problems.rollover-time
  (:import (java.util Calendar
                      GregorianCalendar)))

;; TODO: This maybe should take a parameterized concrete Calendar
(defn ^Calendar inst->cal
  "Given an inst/java.util.Date, return a java.util.Calendar set to that date"
  [inst]
  (doto (GregorianCalendar.)
    (.setTime inst)))

(defn cal->map
  [^Calendar cal]
  {:hours (.get cal Calendar/HOUR)
   :minutes (.get cal Calendar/MINUTE)
   :seconds (.get cal Calendar/SECOND)})

(defn inst->map
  "Given an inst/java.util.Date, return a hash-map of the :hour, :minute, and
  :second components"
  [inst]
  (cal->map (inst->cal inst)))

(defn- cron-f
  "Given a cron-like hash-map and a function to apply to the value vectors,
  return a hash-map {:time-key, f-val}"
  [cron-like f]
  ;; This could also be a zipmap, but I personally like the `reduce`
  (reduce (fn [acc [k v]] (assoc acc k (apply f v))) {} cron-like))

(defn cron-max
  "Given a cron-like hash-map, return the max time slots for each component"
  [cron-like]
  (cron-f cron-like max))

(defn cron-min
  "Given a cron-like hash-map, return the min time slots for each component"
  [cron-like]
  (cron-f cron-like min))

(defn potential-rollovers
  "Returns a set of of keywords, for which their component may rollover"
  [cron inst-map]
  (let [maxes (cron-max cron)]
    (set ;; TODO: it might make more sense to push this `set` out to the consumer
      (keep (fn [[k v]]
              (when (> (inst-map k) (maxes k)) k))
            maxes))))

(def time-order [:hours :minutes :seconds])

(defn highest-rollover
  "Return the highest rollover of a set of potential-rollovers"
  [potent-rollovers]
  (first (filter potent-rollovers time-order)))

(defn select-next*
  "Given a vector of circular step-intervals and a number,
  Return the next appropriate number in the succession"
  [steps n]
  (if-let [step-found (first (drop-while #(> n %) steps))]
    step-found
    (first steps)))

(defn select-next
  [steps n]
  (if (empty? steps)
    (inc n) ;; TODO: this should really only inc if the rollover happened
    (select-next* steps n)))

(defn select-min
  [steps]
  (apply min steps))

(defn select-max
  [steps]
  (apply max steps))

(comment

  (def cron {:minutes [0 15 30 45] :seconds [0 30]})
  (def inst #inst "2014-03-28T11:46:18.766-00:00")

  (inst->map inst)
  (cron-max cron)
  (cron-min cron)

  (def r-overs (potential-rollovers cron (inst->map inst)))
  (:minutes r-overs)
  (:seconds r-overs)
  (highest-rollover r-overs)

  (select-next (:minutes cron) 16)
  (select-next (:hours cron) 7)

  ;; If you have a rollover, you should min the sub-times
  (select-keys
    (cron-min cron)
    (drop-while #(not= (highest-rollover r-overs) %) time-order))

  ;; Here's how you might determine what gets rolled over and what gets inc'd
  (split-with #(not= (highest-rollover r-overs) %) time-order)

  ;; putting it together might look like:
  (let [time-map (inst->map inst)
        [inc-these min-these] (split-with #(not= (highest-rollover r-overs) %) time-order)]
    (merge
      (reduce (fn [acc k] (assoc acc k (select-next (cron k) (time-map k)))) {} inc-these)
      (select-keys (cron-min cron) min-these)))

  ;; And as a function...
  ;; Caution, this still contains bugs, but illustrates one general approach
  (defn next-time
    ([cron]
     (next-time cron (java.util.Date.)))
    ([cron inst]
     (let [time-map (inst->map inst)
           r-overs (potential-rollovers cron time-map)
           [inc-these min-these] (split-with #(not= (highest-rollover r-overs) %) time-order)]
       (merge
         (reduce (fn [acc k] (assoc acc k (select-next (cron k) (time-map k)))) {} inc-these)
         (select-keys (cron-min cron) min-these)))))

  (next-time cron)

  )

