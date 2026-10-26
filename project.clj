(defproject im.chit/ova "1.0.1"
  :description "Stateful arrays for clojure"
  :url "http://github.com/zcaudate/ova"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :java-cmd "/opt/homebrew/opt/openjdk/bin/java"
  :dependencies [[org.clojure/clojure "1.12.6"]
                 [im.chit/hara "1.0.1"]]
  :aliases {"test" ["run" "-m" "code.test"]
            "manage" ["run" "-m" "code.manage"]}
  :profiles {:dev {:dependencies [[xyz.zcaudate/code.test "4.1.8"]
                                  [xyz.zcaudate/code.manage "4.1.8"]]}
             :repl {:plugins [[cider/cider-nrepl "0.63.1"]]}}
  :documentation {:files {"doc/index"
                          {:input "test/midje_doc/ova_guide.clj"
                           :title "ova"
                           :sub-title "stateful arrays for clojure"
                           :author "Chris Zheng"
                           :email  "z@caudate.me"
                           :tracking "UA-31320512-2"}}})
