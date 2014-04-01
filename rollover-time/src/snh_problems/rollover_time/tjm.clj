(ns snh-problems.rollover-time.tjm
  (:require [clj-time.core :as t]))

(defn normalize-schedule
  "Creates a schedule with values for all fields; not just the ones the user
supplied."
  [schedule]
  (let [;; schedule to use for keys that are less than the lowest one defined by
        ;; the user
        single-value-schedule {:year (list (t/year (t/now)))
                               :month [1]
                               :day [1]
                               :hour [0]
                               :min [0]
                               :sec [0]}
        ;; schedule to use for keys that are greater than the lowest one defined
        ;; by the user
        all-values-schedule {:year (range (t/year (t/now)) (Integer/MAX_VALUE))
                             :month (range 1 13)
                             :day (range 1 32)
                             :hour (range 24)
                             :min (range 60)
                             :sec (range 60)}
        ;; split the schedule keys into those that are less than and greater
        ;; than the smallest time unit key defined by the user
        [low-keys hi-keys] (split-with #(not (schedule %))
                                       [:sec :min :hour :day :month :year])]
    (merge-with (fn [left-val right-val]
                  (if (coll? right-val)
                    right-val
                    (list right-val)))
                (-> (select-keys single-value-schedule low-keys)
                    (merge (select-keys all-values-schedule hi-keys)))
                schedule)))

(defn all-events-from
  "Returns a lazy seq of all the scheduled events after the given time for the
given schedule."
  [datetime schedule]
  (let [schedule (normalize-schedule-2 schedule)]
    (drop-while #(t/after? datetime %)
                (for [year (:year schedule)
                      month (:month schedule)
                      day (:day schedule)
                      hour (:hour schedule)
                      minute (:min schedule)
                      second (:sec schedule)
                      ;; since months don't have the same number of days
                      :while (<= day (t/number-of-days-in-the-month year month))]
                  (t/date-time year month day hour minute second)))))

(defn next-event
  "Returns the event that is to occur at or after the given time and schedule."
  [datetime schedule]
  (first (all-events-from datetime schedule)))
