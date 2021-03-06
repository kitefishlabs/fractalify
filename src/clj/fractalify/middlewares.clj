(ns fractalify.middlewares
  (:require
    [com.stuartsierra.component :as c]
    [ring.middleware.reload :as reload]
    [clojure.pprint :refer [pprint]]
    [plumbing.core :as p]
    [ring.middleware.session :as session]
    [ring.middleware.params :as params]
    [ring.middleware.nested-params :as nested-params]
    [ring.middleware.defaults :as defaults]
    [cemerick.drawbridge :as drawbridge]
    [ring.middleware.keyword-params :as keyword-params]
    [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
    [ring.middleware.format :refer [wrap-restful-format]]
    [fractalify.utils :as u]
    [liberator.dev :as ld]
    [cemerick.friend :as frd]
    [ring.middleware.conditional :as mc]))


(defn authenticated? [name pass]
  (= [name pass] [(System/getenv "AUTH_USER") (System/getenv "AUTH_PASS")]))

(defn debug-handler [handler]
  (fn [req]
    (pprint req)
    (handler req)))

(def drawbridge-handler
  (-> (drawbridge/ring-handler)
      (keyword-params/wrap-keyword-params)
      (nested-params/wrap-nested-params)
      (params/wrap-params)
      (session/wrap-session)))

(def ring-defaults (-> defaults/site-defaults
                       (u/dissoc-in [:security :anti-forgery])))

(def repl-url "/repl")
(def api-url "/api")

(defn get-middlewares [handler]
  (-> handler
      (frd/authenticate nil)
      (p/?> u/is-dev? (ld/wrap-trace :header :ui))
      (p/?> u/is-dev? reload/wrap-reload)
      (mc/if-url-starts-with
        api-url
        #(wrap-restful-format % :formats [:transit-json]))

      (mc/if-url-starts-with
        repl-url
        (constantly (wrap-basic-authentication drawbridge-handler authenticated?)))

      (mc/if-url-doesnt-start-with
        repl-url
        #(defaults/wrap-defaults % ring-defaults))))

(defrecord Middlewares []
  c/Lifecycle
  (start [this]
    (assoc this :middlewares get-middlewares))

  (stop [this]
    (dissoc this :middlewares)))

(defn new-middlewares []
  (map->Middlewares {}))