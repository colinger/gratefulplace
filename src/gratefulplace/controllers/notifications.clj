(ns gratefulplace.controllers.notifications
  (:require [ring.util.response :as res]
            [net.cgrand.enlive-html :as h]
            [gratefulplace.models.comment-notification :as notification]
            [gratefulplace.views.notifications :as view])

  (:use gratefulplace.controllers.common
        gratefulplace.models.permissions))

(defn all
  [params]
  (view
   view/all
   :notifications (if (current-user-id)
                    (let [html (notification/all (current-user-id))]
                      (notification/mark-all-viewed (current-user-id))
                      html)
                    [])))