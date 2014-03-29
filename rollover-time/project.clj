(defproject rollover-time "0.1.0-SNAPSHOT"
  :description "Modeling fall-through state in Clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :global-vars  {*warn-on-reflection* true
                 *assert* true}
  :pedantic? :abort
  :jvm-opts ^:replace [])

