(ns gratefulplace.models.post
  (:require gratefulplace.models.db
            [gratefulplace.models.entities :as e])
  (:use korma.core
        gratefulplace.utils))

(def validations
  {:content
   ["Whoops! You forgot to write anything"
    #(> (count %) 1)]})

(defn create!
  [attributes]
  (insert e/post (values attributes)))

(defn update!
  [conditions attributes]
  (let [attributes (dissoc attributes :id)]
    (update e/post
            (set-fields attributes)
            (where (str->int conditions :id)))))

(defmacro all
  [conditions]
  `(select e/post
           (with e/user
                 (fields :username))
           (with e/comment
                 (aggregate (~'count :*) :count)
                 (where {:hidden false}))
           (with e/favorite
                 (aggregate (~'count :*) :count))
           (where ~conditions)
           (order :created_on :DESC)))

(defmacro record-count
  [conditions]
  `(select e/post
    (aggregate (~'count :*) :cnt)
    (where ~conditions)))

(defn by-id
  [id]
  (first (select e/post
                 (with e/user
                       (fields :username))
                 (with e/favorite
                       (aggregate (count :*) :count))
                 (where {:id (str->int id)}))))
