(ns fractalify.fractals.handlers
  (:require-macros [fractalify.tracer-macros :refer [trace-handlers]])
  (:require [fractalify.middleware :as m]
            [re-frame.core :as f]
            [fractalify.fractals.lib.l-systems :as l]
            [fractalify.fractals.lib.renderer :as renderer]
            [cljs.core.async :refer [<!]]
            [fractalify.utils :as u]
            [workers.core :as w]
            [fractalify.tracer]))


(def worker (new js/Worker "/js/turtle-worker.js"))

(trace-handlers
  (f/register-handler
    :l-system-change
    m/standard-middlewares
    (fn [db [canvas-dom l-system]]
      (let [db (assoc-in db [:l-system-generating] true)]
        (f/dispatch [:generate-cmds canvas-dom l-system])
        db)))

  (f/register-handler
    :generate-cmds
    m/standard-middlewares
    (fn [db [canvas-dom l-system]]
      (println ":generate-cmds")
      (let [result-cmds (l/l-system l-system)]
        (w/on-message-once #(f/dispatch [:on-lines-generated canvas-dom %]) worker)
        (w/post-message [l-system result-cmds] worker))
      db))

  (f/register-handler
    :on-lines-generated
    m/standard-middlewares
    (fn [db [canvas-dom lines]]
      (println ":on-lines-generated")
      (renderer/render! canvas-dom lines)
      (assoc-in db [:l-system-generating] false))))





