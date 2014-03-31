(ns snh-problems.rollover-time.tjm
  (:require [clj-time.core :as t]))

(defn normalize-schedule
  "Creates a schedule with value for all fields; not just the ones the user
supplied."
  [schedule]
  (merge {:year (range 2014 (Integer/MAX_VALUE))
          :month (range 1 13)
          :day (range 1 32)
          :hour [0]
          :min [0]
          :sec [0]}
         schedule))

(defn all-events-from
  "Returns a lazy seq of all the scheduled events after the given time for the
given schedule."
  [datetime schedule]
  (let [schedule (normalize-schedule schedule)]
    (drop-while #(t/after? datetime %)
                (for [year (:year schedule)
                      month (:month schedule)
                      day (:day schedule)
                      hour (:hour schedule)
                      minute (:min schedule)
                      second (:sec schedule)
                      :while (<= day (t/number-of-days-in-the-month year month))]
                  (t/date-time year month day hour minute second)))))

(defn next-event
  "Returns the event that is to occur at or after the given time and schedule."
  [datetime schedule]
  (first (all-events-from datetime schedule)))
