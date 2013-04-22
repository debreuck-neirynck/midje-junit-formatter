(ns ^{:doc "JUnit formatter for Midje output"}
  midje-junit-formatter.core
  (:use midje.emission.util)
  (:require [midje.data.fact :as fact]
            [midje.config :as config]
            [midje.emission.state :as state]
            [midje.emission.plugins.util :as util]
            [midje.emission.plugins.silence :as silence]
            [midje.emission.plugins.default-failure-lines :as lines]
            [clojure.string :as str]
            [clojure.xml :as xml :only [emit-element]]))

(def report-file "report.xml")

(defn log-fn []
  (fn [text] (spit report-file text :append true)))

(defn- log [string]
  (let [log-fn (log-fn)]
    (log-fn string)))

(defn- reset-log []
  (spit report-file ""))

(def last-fact (atom {}))

(defn- fact-name [fact]
  (or (fact/name fact)
      (fact/description fact)
      (str (fact/file fact) ":" (fact/line fact))))

(defn pass []
  (log
    (with-out-str
      (xml/emit-element @last-fact))))

(defn- testcase-with-failure [failure-map]
  (let [testcase @last-fact
        failure-content (str "<![CDATA[" (apply str (lines/summarize failure-map)) "]]>")
        fail-type (:type failure-map)
        fail-element {:tag :failure
                      :content [failure-content]
                      :attrs {:type fail-type}}
        testcase-with-failure (assoc testcase :content [fail-element])]
    testcase-with-failure))

(defn escape [s]
  (if s
    (str/escape s {\' "\\'"})
    ""))

(defn fail [failure-map]
  (let [testcase (testcase-with-failure failure-map)]
    (log
      (with-out-str
        (xml/emit-element testcase)))))

(defn starting-to-check-fact [fact]
  (let [fact-namespace (str (fact/namespace fact))
        fact-name (fact-name fact)]
    (reset! last-fact {:tag :testcase
                       :attrs {:classname (escape fact-namespace) :name (escape fact-name)}})))

(defn starting-fact-stream []
  (reset-log)
  (log "<testsuite>"))

(defn finishing-fact-stream [midje-counters clojure-test-map]
  (log "</testsuite>"))

(defn make-map [& keys]
  (zipmap keys
          (map #(ns-resolve *ns* (symbol (name %))) keys)))

(def emission-map (merge silence/emission-map
                         (make-map :fail
                                   :pass
                                   :starting-fact-stream
                                   :finishing-fact-stream
                                   :starting-to-check-fact)))

(state/install-emission-map emission-map)
