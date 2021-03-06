(ns kdiff.core
  (:require [cli-matic.core :as matic]
            [kdiff.diff :as diff]
            [cli-matic.utils-v2 :as U2])
  (:gen-class))

(defn diff [{:keys [old new] :as opts}]
  (diff/view old new (dissoc opts :old :new)))

(defn generate-config []
  {:command     "kdiff"
   :description "A tool for diffing kubernetes manifests."
   :version     "0.0.1"
   :subcommands [{:command     "diff"
                  :description "Diff kubernetes resources across files / directories."
                  :examples    ["kdiff diff --old old.yml --new new.yml"
                                "kdiff diff --old old/ --old also-old/file.yml --new new/ --new also-new/"]
                  :opts        [{:as       "An old (expected) manifest file or directory."
                                 :option   "old"
                                 :short    "o"
                                 :type     :string
                                 :multiple true
                                 :default  :present}
                                {:as       "An new (actual) manifest file or directory."
                                 :option   "new"
                                 :short    "n"
                                 :type     :string
                                 :multiple true
                                 :default  :present}
                                {:as      "Skip displaying diff for entirely new resources."
                                 :option  "ignore-added-resources"
                                 :short   "iar"
                                 :type    :flag
                                 :default false}
                                {:as      "Skip displaying diff for entirely removed resources."
                                 :option  "ignore-removed-resources"
                                 :short   "irr"
                                 :type    :flag
                                 :default false}]
                  :runs        diff}]})

(defn repl [& args]
  (-> (generate-config)
      (U2/cfg-v2)
      (matic/run-cmd* args)))

(defn -main [& args]
  (matic/run-cmd args (generate-config)))