(ns gratefulplace.views.common
  (:require [net.cgrand.enlive-html :as h]
            markdown)
  (use [cemerick.friend :only (current-authentication)]
       gratefulplace.utils))

(defonce *template-dir* "gratefulplace/templates/")

(h/defsnippet nav (str *template-dir* "index.html") [:nav]
  [logged-in]
  [:.auth] (if logged-in
             (h/do-> (h/content "Log Out")
                     (h/set-attr :href "/logout"))
             (h/do-> (h/content "Log In")
                     (h/set-attr :href "/login"))))

(h/deftemplate layout (str *template-dir* "index.html")
  [html]
  [:html] (h/substitute html)
  [:nav] (h/substitute (nav (current-authentication)))

  [:nav :ul.secondary :#logged-in :a]
  (if-let [username (:username (current-authentication))]
    (h/do->
     (h/content username)
     (h/set-attr :href (str "/users/" username))))
  
  [:nav :ul.secondary :#logged-in :span]
  (when (current-authentication)
    (h/content "Logged in as")))


;; TODO here's another refactoring! WooooOOO
(defn user-path
  [x]
  (let [username (or (:username x) x)]
    (str "/users/" username)))

(defn set-user-path
  [x]
  (h/set-attr :href (user-path x)))

(defn post-path
  [x]
  (let [id (or (:id x) x)]
    (str "/posts/" id)))

(defn set-post-path
  [x]
  (h/set-attr :href (post-path x)))

(defn comment-path
  [x]
  (let [id (or (:id x) x)]
    (str "/comments/" id)))

(defn set-comment-path
  [x]
  (h/set-attr :href (comment-path x)))

;; Need to come up with better name
;; Bundles together some defsnippet commonalities for user with the
;; layout template
;;
;; TODO destructuring doesn't work in argnames
(defmacro defpage
  [name file [& argnames] & body]
  `(do
     (h/defsnippet ~(symbol (str name "*")) (str *template-dir* ~file) [:html]
       [~@argnames]
       ~@body)
     (defn ~name
       [~@argnames]
       (layout (~(symbol (str name "*")) ~@argnames)))))

(defn md-content
  [content]
  (let [content (if-let [c (:content content)] c content)]
    (h/html-content (markdown/md-to-html-string content))))

(defn format-error-messages
  [errors]
  (str "<ul>" (apply str (map #(str "<li>" % "</li>") errors)) "</ul>"))

(defn error-content
  [errors, k]
  (if-let [messages (k errors)]
    (h/html-content (format-error-messages messages))))

(defn relation-count
  [record relation-key]
  (get-in record [relation-key 0 :count] 0))

(defn linked-username
  [record]
  (h/do->
   (h/content (:username record))
   (set-user-path record)))

(defn timestamp->string
  [timestamp]
  (-> (java.text.SimpleDateFormat. "MMM dd, yyyy hh:mma")
      (.format timestamp)))

(defn created-on
  [x]
  (timestamp->string (:created_on x)))

(defmacro keep-when
  [condition]
  `(when ~condition
     #(identity %)))