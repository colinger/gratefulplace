(ns gratefulplace.models.post
  (:require gratefulplace.models.db
            [gratefulplace.models.entities :as e])
  (:use korma.core
        gratefulplace.utils))

(defn create!
  [attributes]
  (insert e/post (values attributes)))

;; TODO refactor - looks similar to comments
(defn all
  ([]
     (all {}))
  ([conditions]
     ;; TODO fix this... shouldn't have to put in a bogus where
     (let [conditions (if (empty? conditions) true conditions)]
       (select e/post
               (with e/user
                     (fields :username))
               (with e/comment
                     (aggregate (count :*) :count))
               (where conditions)
               (order :created_on :DESC)))))

(defn by-id
  [id]
  (first (select e/post
                 (with e/user
                       (fields :username))
                 (with e/comment
                       (fields :content :created_on)
                       (with e/user
                             (fields :username)))
                 (where {:id (str->int id)}))))