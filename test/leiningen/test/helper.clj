(ns leiningen.test.helper
  (:require [lancet.core :as lancet])
  (:use [clojure.java.io :only [file]]
        [leiningen.compile :only [platform-nullsink]]
        [leiningen.core :only [read-project]])
  (:require [clojure.xml :as xml]
			[clojure.zip :as zip]
			[clojure.contrib.zip-filter.xml :as zfx])
  (:import java.io.File)
  )

(def local-repo
	 (let [home-maven (file (System/getProperty "user.home") ".m2")
		   home-settings (if (.exists home-maven) (file home-maven "settings.xml"))
		   settings-repo (if (and home-settings (.exists home-settings))
				            (-> home-settings .getPath xml/parse zip/xml-zip (zfx/xml1-> :localRepository zfx/text)))
		   ]
	   (if settings-repo
		  (file settings-repo)
		  (file home-maven "repository")
		 )
	   )
	 )

(defn m2-dir [n v]
  (file local-repo (if (string? n) n (or (namespace n) (name n))) (name n) v))

(defn- read-test-project [name]
  (binding [*ns* (find-ns 'leiningen.core)]
    (read-project (format "test_projects/%s/project.clj" name))))

(def sample-project (read-test-project "sample"))

(def dev-deps-project (read-test-project "dev-deps-only"))

(def sample-failing-project (read-test-project "sample_failing"))

(def sample-no-aot-project (read-test-project "sample_no_aot"))

(def tricky-name-project (read-test-project "tricky-name"))

(def native-project (read-test-project "native"))

(def logger (first (.getBuildListeners lancet/ant-project)))

(defmacro with-no-log [& body]
  `(do (.setOutputPrintStream logger (platform-nullsink))
       (.setErrorPrintStream logger (platform-nullsink))
       (try ~@body
            (finally (.setOutputPrintStream logger System/out)
                     (.setErrorPrintStream logger System/err)))))
