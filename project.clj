(defproject liu.mars/akka-stream-clojure "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :test-paths ["src/test/clojure"]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [liu.mars/akka-clojure "0.1.2"]
                 [com.typesafe.akka/akka-actor_2.12 "2.5.19"]
                 [com.typesafe.akka/akka-stream_2.12 "2.5.19"]
                 [liu.mars/jaskell "0.2.1"]]
  :profiles {:test {:dependencies [[com.typesafe.akka/akka-testkit_2.12 "2.5.19"]]}})