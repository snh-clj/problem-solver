#!/usr/bin/env boot

#tailrecursion.boot.core/version "2.2.1"

(set-env!
  :project      'snh-clj/rollover-time
  :version      "0.1.0-SNAPSHOT"
  :description  "Modeling fall-through state in Clojure"
  :license      {:name  "Eclipse Public License"
                 :url   "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies '[[tailrecursion/boot.task "2.1.1"]]
  :src-paths    #{"src"})

(require '[tailrecursion.boot.task :refer :all])

(deftask lein-tests
  "Run the tests via lein"
  []
  (lein "test"))

