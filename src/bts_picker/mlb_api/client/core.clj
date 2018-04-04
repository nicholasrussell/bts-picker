(ns bts-picker.mlb-api.client.core
  (:require [clj-http.client :as http-client]
            [cheshire.core :as cheshire]
            [clojure.tools.trace :as trace]))

(def ^:private debug true)
(def ^:private base-url "http://statsapi.mlb.com/api")

(defn- make-url
  [path]
  (format "%s/%s" base-url path))

(defn get
  ([path]
   (get path {}))
  ([path {:keys [query-params]}]
   ; TODO handle exceptions
   (some->
     (http-client/get
       (make-url path)
       {:query-params query-params
        :as :json
        :debug debug})
     :body)))
