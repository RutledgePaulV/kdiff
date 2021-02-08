(ns kdiff.diff
  (:require [clojure.string :as strings]
            [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [lambdaisland.deep-diff2 :as diff]
            [clojure.set :as sets]
            [clojure.walk :as walk])
  (:import [java.io File]))

(defn normalize [s]
  (walk/postwalk
    (fn [form]
      (if (seq? form)
        (into [] form)
        form))
    s))

(defn parse [file]
  (try
    (->> (strings/split (slurp file) #"---\R*")
         (remove strings/blank?)
         (map yaml/parse-string)
         (map normalize)
         (mapv #(with-meta % {:file file})))
    (catch Exception e
      (println "Error parsing yaml from file " (.getAbsolutePath ^File file) ". Proceeding as though no resources were found in the file.")
      (.printStackTrace e)
      [])))

(defn identifier [resource]
  (with-meta
    {:kind      (get-in resource [:kind])
     :namespace (get-in resource [:metadata :namespace])
     :name      (get-in resource [:metadata :name])}
    (meta resource)))

(defn yaml-file? [^File f]
  (and (.isFile f)
       (.canRead f)
       (or (strings/ends-with? (.getName f) ".yml")
           (strings/ends-with? (.getName f) ".yaml"))))

(defn load-directory [dir]
  (if (coll? dir)
    (reduce merge {} (map load-directory dir))
    (->> (file-seq (io/file dir))
         (filter yaml-file?)
         (mapcat parse)
         (into {} (map (juxt identifier identity))))))


; this should really be a protocol but i was running into issues
; i wasn't able to resolve with aot, protocols, and changing
; class definitions of the lambdaisland records
(defn minimize-diff [x]
  (letfn [(walk-seq [x]
            (tree-seq #(and (not (string? %)) (seqable? %)) seq x))
          (seek
            ([pred coll]
             (seek pred coll nil))
            ([pred coll not-found]
             (reduce (fn [nf x] (if (pred x) (reduced x) nf)) not-found coll)))
          (diff? [x]
            (and (some? x) (strings/starts-with? (.getName (class x)) "lambdaisland.deep_diff2.")))
          (contains-diff-form? [x]
            (->> (walk-seq x) (seek diff?) (some?)))]
    (let [xform (comp (filter contains-diff-form?) (map minimize-diff))]
      (cond
        (diff? x)
        x
        (vector? x)
        (into [] xform x)
        (set? x)
        (into #{} xform x)
        (map? x)
        (into {} (keep
                   (fn [[k v]]
                     (case [(contains-diff-form? k) (contains-diff-form? v)]
                       [false false] nil
                       [true false] [k v]
                       [true true] [(minimize-diff k) (minimize-diff v)]
                       [false true] [k (minimize-diff v)])))
              (seq x))
        :otherwise x))))

(defn view
  ([old-dir new-dir]
   (view old-dir new-dir {}))
  ([old-dir new-dir {:keys [ignore-added-resources ignore-removed-resources]
                     :or   {ignore-added-resources   false
                            ignore-removed-resources false}}]
   (let [old (load-directory old-dir)
         new (load-directory new-dir)]
     (doseq [k (sort-by (juxt :kind :namespace :name) (sets/union (set (keys old)) (set (keys new))))
             :let [added?   (not (contains? old k))
                   removed? (not (contains? new k))
                   changed? (and (not added?) (not removed?))]
             :when
             (or changed?
                 (and added? (false? ignore-added-resources))
                 (and removed? (false? ignore-removed-resources)))]
       (let [diff (minimize-diff (diff/diff (or (get old k) {}) (or (get new k) {})))]
         (when (not-empty diff)
           (println (format "kind: %s, namespace: %s, name: %s" (:kind k) (or (:namespace k) "n/a") (:name k)))
           (when-some [file (some-> (get old k) meta :file)]
             (println (format "old: %s" (.getAbsolutePath ^File file))))
           (when-some [file (some-> (get new k) meta :file)]
             (println (format "new: %s" (.getAbsolutePath ^File file))))
           (diff/pretty-print diff)
           (println)
           (flush)))))))

