(ns midje-junit-formatter.core-test
  (:require
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]
    [midje-junit-formatter.test-util :refer :all]
    [midje-junit-formatter.core :as plugin]
    [midje.config :as config]
    [midje.emission.plugins.default-failure-lines :as failure-lines]))

(testable-privates midje-junit-formatter.core dissoc-in)

(facts "about `dissoc-in`"
       (fact "removes item from map when single level"
             (dissoc-in {:key :value} [:key]) => {})
       
       (fact "removes last part of path in tree"
             (dissoc-in {:a {:b :c}} [:a :b]) => {:a {}}))

(defn innocuously [key & args]
  (config/with-augmented-config {:emitter 'midje-junit-formatter.core
                                 :print-level :print-facts}
    (captured-output (apply (key plugin/emission-map) args))))

(def test-fact
  (with-meta (fn[]) {:midje/name "named" :midje/description "desc" :midje/namespace "blah"}))

(def test-failure-map
 {:type :some-prerequisites-were-called-the-wrong-number-of-times,
   :namespace "midje.emission.plugins.t-junit"})

#_(fact "starting a fact stream opens a <testsuite>"
  (innocuously :starting-fact-stream) => (contains "<testsuite>")
  (provided
    (plugin/log-fn) => #(println %)))

#_(fact "closing a fact stream closes </testsuite>"
  (innocuously :finishing-fact-stream {} {}) => (contains "</testsuite>")
  (provided
    (plugin/log-fn) => #(println %)))

(facts "about `starting-to-check-fact`"
       (against-background
        [(plugin/log-fn) => #(println %)]
        
        (fact "pass produces a <testcase> tag"
              (plugin/starting-to-check-fact test-fact)

              (innocuously :pass) => (contains "<testcase"))

        (fact "facts have an elapsed time"
              (plugin/starting-to-check-fact test-fact)
              (Thread/sleep 3)
              (plugin/finishing-fact test-fact)

              (innocuously :pass) => (contains "time='"))

        (fact "failure produces a <failure> tag"
              (plugin/starting-to-check-fact test-fact)

              (innocuously :fail test-failure-map) => (contains "<failure type=':some-prerequisites-were-called-the-wrong-number-of-times'>"))
        
        (fact "escapes test name"
              (let [fact (with-meta (fn[]) {:midje/name "key & value"
                                            :midje/description "desc"
                                            :midje/namespace "blah"})]
                (plugin/starting-to-check-fact fact)
                (innocuously :pass) => (contains "key &amp; value")))
        
        (fact "does not contain test description"
              (let [fact (with-meta (fn[]) {:midje/name "test-name"
                                            :midje/description "test-desc"
                                            :midje/namespace "blah"})]
                (plugin/starting-to-check-fact fact)
                (innocuously :pass) =not=> (contains "test-desc")))))
