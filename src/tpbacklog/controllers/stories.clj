(ns tpbacklog.controllers.stories
  (:use [compojure.core :only (defroutes POST GET PUT DELETE)])
  (:require [tpbacklog.db :as db]))

(def DB_SUBSPACE "stories")

(defn- r-create [points priority title]
  {:pre [(number? points)
         (number? priority)
         (not (nil? title))]
   :post [(number? %)]}
  (let [id (db/next-id DB_SUBSPACE)
        story {:points points :priority priority :title title}]
    (db/set-rec DB_SUBSPACE id story)
    id))

(defn- r-read [id]
  {:pre [(number? id)]
   :post [(not (nil? %))]}
  (db/get-rec DB_SUBSPACE id))

(defn- r-update [id points priority title]
  {:pre [(number? id)
         (number? points)
         (number? priority)
         (not (nil? title))]}
  (let [story {:points points :priority priority :title title}]
    (db/set-rec DB_SUBSPACE id story)))

(defn- r-delete [id]
  {:pre [(number? id)]}
  (db/del-rec DB_SUBSPACE id))

(defroutes routes
  (POST "/stories" [points priority title]
    (try
      (let [points (Integer/parseInt points)
            priority (Integer/parseInt priority)
            id (r-create points priority title)]
        {:status 201
         :headers {
           "Location" (str "/stories/" id)}})
      (catch java.lang.NumberFormatException e {:status 400}) ; story invalid
      (catch java.lang.AssertionError e {:status 400}))) ; story invalid
  (GET "/stories/:id" [id]
    (try
      (let [id (Integer/parseInt id)
            story (r-read id)]
        {:status 200 :body story})
      (catch java.lang.NumberFormatException e {:status 404}) ; id invalid
      (catch java.lang.AssertionError e {:status 404}))) ; id invalid
  (PUT "/stories/:id" [id points priority title]
    (try
      (let [id (Integer/parseInt id)]
        (r-read id) ; check exists
        (try
          (let [points (Integer/parseInt points)
                priority (Integer/parseInt priority)]
            (r-update id points priority title)
            {:status 204})
          (catch java.lang.NumberFormatException e {:status 400}) ; story invalid
          (catch java.lang.AssertionError e {:status 400}))) ; story invalid
      (catch java.lang.NumberFormatException e {:status 404}) ; id invalid
      (catch java.lang.AssertionError e {:status 404}))) ; id invalid
  (DELETE "/stories/:id" [id]
    (try
      (let [id (Integer/parseInt id)]
        (r-read id) ; check exists
        (r-delete id)
        {:status 204})
      (catch java.lang.NumberFormatException e {:status 404}) ; id invalid
      (catch java.lang.AssertionError e {:status 404})))) ; id invalid