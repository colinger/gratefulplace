(ns gratefulplace.views.posts
  (:require [net.cgrand.enlive-html :as h]
            markdown)
  (:use [gratefulplace.views.common :exclude [layout nav *template-dir*]]
        gratefulplace.utils
        gratefulplace.models.permissions
        [cemerick.friend :only (current-authentication)]))

(defn comments
  [post]
  (let [comment-count (get-in post [:comment 0 :count] 0)]
    (if (zero? comment-count)
      "Comment"
      (str  comment-count " comments"))))

(defn favorite
  [current-auth post]
  (fn [node]
    (if (and
         current-auth
         (contains? (user-favorites (:id current-auth)) (:id post)))
      (-> node
          ((set-path post favorite-destroy-path))
          ((h/add-class "added")))
      ((set-path post favorite-path) node))))

(defpage all "index.html"
  [posts current-auth]
  ;; don't show the second post as it's just an example
  [[:.post (h/nth-of-type 2)]] nil
  [:.post] (h/clone-for
            [post posts]
            [:.author :a] (linked-username post)
            [:.date]      (h/content (created-on post))
            [:.content]   (md-content post)
            [:.comments]  (h/do->
                           (h/content (comments post))
                           (set-path post post-path))
            
            [:.favorite] (favorite current-auth post)))

(defpage show-new "posts/new.html"
  [params errors current-auth]
  [:#content :textarea] (h/content (:content params))
  [:#content :.errors] (if current-auth
                         (error-content errors :content)
                         (h/html-content "You'll need to <a href=\"/login\">log in</a> to post")))

(defpage show "posts/show.html"
  [post comments current-auth]
  [:.post :.author]      (linked-username post)
  [:.post :.date]        (h/content (created-on post))
  [:.post :.content]     (md-content post)

  [:.post :.edit]        (keep-when (can-modify-record? post))
  [:.post :.edit :a]     (set-path post post-edit-path)

  

  [:.post :.moderate]    (keep-when (moderator? (:username current-auth)))
  [:.post :.moderate :a] (h/do->
                          (set-path post post-path)
                          (h/content (if (:hidden post) "unhide" "hide")))
  
  [:#post_id]            (h/set-attr :value (:id post))
  [:.favorite]           (favorite current-auth post)

  [:.comments :.comment]
  (h/clone-for [comment comments]
               [:.content]   (h/set-attr :id (str "comment-" (:id comment)))
               [:.author :a] (linked-username comment)
               [:.date]      (h/content (created-on comment))
               [:.content]   (md-content comment)

               [:.moderate]  (keep-when (moderator? (:username current-auth)))
               [:.moderate :a] (h/do->
                                (set-path comment comment-path)
                                (h/content (if (:hidden comment) "unhide" "hide")))

               [:.edit]      (keep-when (can-modify-record? comment current-auth))
               [:.edit :a]   (set-path comment comment-edit-path)))

(defpage edit "posts/edit.html"
  [post]
  [:form]     (h/set-attr :action (post-path post))
  [:textarea] (h/content  (:content post)))