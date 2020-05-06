(defproject dn/midje-junit-formatter "0.1.1"
  :description "Formats the midje output to a JUnit-style XML for CI/CD purposes"
  :url "https://www.d-n.be"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [midje "1.9.9"]
                 [clojure.java-time "0.3.2"]]
  :profiles {:dev {:plugins [[lein-midje "3.2.1"]]}}
  :deploy-repositories
  [["snapshots" {:url "https://nexus.d-n.be/repository/maven-snapshots"
                 :username [:gpg :env/nexus_user]
                 :password [:gpg :env/nexus_pass]}]
   ["releases"  {:url "https://nexus.d-n.be/repository/maven-releases/"
                 :username [:gpg :env/nexus_user]
                 :password [:gpg :env/nexus_pass]
                 :sign-releases false}]
   ["dn-clojars" {:url "https://clojars.org/repo"
                  :username [:gpg :env/clojars_user]
                  :password [:gpg :env/clojars_pass]}]])
