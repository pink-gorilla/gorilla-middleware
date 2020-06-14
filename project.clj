(defproject org.pinkgorilla/gorilla-middleware "0.2.25-SNAPSHOT"
  :description "Pink Gorilla nREPL middleware"
  :url "https://github.com/pink-gorilla/gorilla-middleware"
  :license {:name "MIT"}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/release_username
                                     :password :env/release_password
                                     :sign-releases false}]]
  :dependencies  [[org.clojure/clojure "1.10.1"]
                  [org.clojure/spec.alpha "0.2.187"]
                  [org.clojure/data.json "0.2.6"]
                  [com.stuartsierra/component "0.4.0"]
                  [jarohen/chord "0.8.1" ; nrepl websocket
                   :exclusions [com.cognitect/transit-clj
                                com.cognitect/transit-cljs]] ; websockets with core.async
                  [nrepl "0.6.0"]
                  [cider/cider-nrepl "0.22.4"]
                  [cider/piggieback "0.4.2"] 
                  [clojail "1.0.6"] ; sandboxing
                  [org.pinkgorilla/gorilla-renderable "3.0.5"]]

  :profiles {:dev   {:dependencies [[clj-kondo "2019.11.23"]]
                     :plugins [[lein-cljfmt "0.6.6"]
                               [lein-cloverage "1.1.2"]]
                     :aliases {"clj-kondo" ["run" "-m" "clj-kondo.main"]}
                     :cloverage {:codecov? true
                                 ;; In case we want to exclude stuff
                                 ;; :ns-exclude-regex [#".*util.instrument"]
                                 ;; :test-ns-regex [#"^((?!debug-integration-test).)*$$"]
                                 }
                     ;; TODO : Make cljfmt really nice : https://devhub.io/repos/bbatsov-cljfmt
                     :cljfmt  {:indents {as->                [[:inner 0]]
                                         with-debug-bindings [[:inner 0]]
                                         merge-meta          [[:inner 0]]
                                         try-if-let          [[:block 1]]}}}}

  :aliases {"bump-version" ["change" "version" "leiningen.release/bump-version"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["bump-version" "release"]
                  ["vcs" "commit" "Release %s"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["deploy"]
                  ["bump-version"]
                  ["vcs" "commit" "Begin %s"]
                  ["vcs" "push"]]

  :repl-options {:init-ns pinkgorilla.middleware.cljs})
