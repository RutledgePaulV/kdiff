(defproject org.clojars.rutledgepaulv/kdiff "0.1.0-SNAPSHOT"

  :plugins
  [[io.taylorwood/lein-native-image "0.3.1"]]

  :dependencies
  [[org.clojure/clojure "1.10.2"]
   [lambdaisland/deep-diff2 "2.0.108"]
   [clj-commons/clj-yaml "0.7.2"]
   [cli-matic "0.4.3"]]

  :profiles
  {:uberjar
   {:aot :all
    :native-image
         {:jvm-opts ["-Dclojure.compiler.direct-linking=true"
                     "-Dclojure.spec.skip-macros=true"]
          :opts     ["--report-unsupported-elements-at-runtime"
                     "--initialize-at-build-time"]
          :name     "kdiff"}}}

  :aliases
  {"build" ["with-profile" "+uberjar" "native-image"]}

  :main
  ^:skip-aot
  kdiff.core

  :repl-options
  {:init-ns kdiff.core})
