(defproject xyz.zcaudate/ova "1.0.1"
  :description "Stateful arrays for clojure"
  :url "http://github.com/zcaudate/ova"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :java-cmd ~(or (System/getenv "JAVA_CMD")
                 (when-let [java-home (System/getenv "JAVA_HOME")]
                   (str java-home "/bin/java"))
                 "/opt/homebrew/opt/openjdk/bin/java")
  :dependencies [[org.clojure/clojure "1.12.6"]]
  :aliases {"test" ["run" "-m" "code.test"]
            "manage" ["run" "-m" "code.manage"]
            "docs" ["run" "-m" "ova-documentation.build"]
            "docs-check" ["run" "-m" "ova-documentation.build" "check"]}
  :profiles {:dev {:dependencies [[xyz.zcaudate/code.test "4.1.8"]
                                  [xyz.zcaudate/code.manage "4.1.8"]
                                  [xyz.zcaudate/code.doc "4.1.8"]]
                   :source-paths ["src-documentation"]}
             :repl {:plugins [[cider/cider-nrepl "0.63.1"]]}}
  :publish "config/publish.edn")
