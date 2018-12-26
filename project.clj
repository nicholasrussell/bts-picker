(defproject bts-picker "0.1.0"
  :description "A tool to make MLB Beat the Streak picks for me"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.9.1"]
                 [org.clojure/data.csv "0.1.4"]
                 [reaver "0.1.2"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/tools.trace "0.7.10"]
                 [cheshire "5.8.1"]
                 [org.clojure/test.check "0.10.0-alpha3"]
                 [com.gfredericks/test.chuck "0.2.9"]]
  :managed-dependencies [[org.clojure/core.rrb-vector "0.0.13"]]
  :main bts-picker.core)
