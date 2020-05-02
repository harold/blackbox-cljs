(defproject blackbox-cljs "0.1.0-SNAPSHOT"
  :description "Like Emacs' blackbox, but in cljs in the browser."
  :url "https://github.com/harold/blackbox-cljs"
  :license {:name "Copyright 2020, Harold"
            :url "https://github.com/harold/blackbox-cljs"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [com.bhauman/figwheel-main "0.2.4"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]
                 [reagent "0.10.0"]]
  :resource-paths ["target" "resources"]
  :aliases {"build-prod" ["trampoline" "run" "-m" "figwheel.main" "-bo" "prod"]}
  :clean-targets ^{:protect false} [:target-path])
