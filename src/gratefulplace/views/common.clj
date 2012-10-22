(ns gratefulplace.views.common
  (:require [net.cgrand.enlive-html :as h]))

(defonce *template-dir* "gratefulplace/templates/")

(h/defsnippet nav (str *template-dir* "index.html") [:nav]
  [logged-in]
  [:.auth] (h/content (if logged-in "Log Out" "Log In")))

(h/deftemplate layout (str *template-dir* "index.html")
  [html]
  [:html] (h/substitute html)
  [:nav] (h/substitute (nav false)))

;; Need to come up with better name
;; Bundles together some defsnippet commonalities for user with the
;; layout template
(defmacro defpage
  [name file [& argnames] & body]
  `(do
     (h/defsnippet ~(symbol (str name "*")) (str *template-dir* ~file) [:html]
       [~@argnames]
       ~@body)
     (defn ~name
       [~@argnames]
       (layout (~(symbol (str name "*")) ~@argnames)))))
