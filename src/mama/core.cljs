(ns mama.core
  (:require [reagent.core :as r]
            [reagent.dom :as d]
            [ajax.core :refer [GET POST]]
            [clojure.core.async :as a]
            [reitit.core :as reitit]
            [clojure.string :as string]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [mama.components.documents.editor :as editor])
  (:import goog.History))

(defonce session (r/atom {:page :home}))
(defonce task (r/atom {}))
(defonce dashboard (r/atom {}))

(defn searchbar []
  [:div.search-bar
   [:input {:type "text" :value "search"}]])

(def doc-icon "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\">
<path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z\" /></svg><p class=\"icon-text\">New Doc</p>         ")

(def task-icon "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\">
  <path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4\" /></svg><p class=\"icon-text\">New Task</p>     ")

(def table-icon "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\">
<path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M3 10h18M3 14h18m-9-4v8m-7 0h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z\" /></svg>")

(def template-icon "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\">
  <path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z\" /></svg><p class=\"icon-text\">Dashboard</p>")

(defn sidebar []
  [:div.sidenav
   [:a {:href "#/" :dangerouslySetInnerHTML {:__html template-icon}}]
   [:a {:href "#/documents" :dangerouslySetInnerHTML {:__html doc-icon}}]
   [:a {:href "#" :dangerouslySetInnerHTML {:__html task-icon}}]])

(defn dashboard-widget
  [title body link]
  [:div.card
   [:a {:href link}
    [:h3.widget-title title]
    [:p.task-body body]]])

(defn dashboard-task
  [title body link]
  [:div.card.task
   [:a {:href link}
    [:h3.task-title title]
    [:hr]
    [:p.task-body  body]]])

(defn dashboard-page []
  (let [widgets (get-in @dashboard ["Widgets"])
        task-name (get @task "Name")
        task-description (get @task "Description")]
    (js/console.log widgets)
    (js/console.log  @task)
    [:section.section
     (into [:div.container
            [dashboard-task task-name task-description ""]]
           (map (fn [widget]
                  (js/console.log widget)
                  [dashboard-widget (get widget "title") (take 120 (get widget "Preview")) ""]) widgets))]))


(defn hi []
  [:h1 "hi"])

;; -------------------------
;; Views

(defn home-page []
  (do
    (GET "http://localhost:8080/tasks" {:params {:task_id 4
                                                 :org_id 1}
                                        :handler (fn [req]
                                                   (reset! task req))})
    (GET "http://localhost:8080/dashboard" {:params {:task_id 4
                                                     :org_id 1}
                                            :handler (fn [req]
                                                       (reset! dashboard req))})
    [dashboard-page]))

;; -------------------------
;; Initialize app

(def pages
  {:home #'home-page
   :documents #'editor/document-editor})

(defn page []
  [(pages (:page @session))])

(def router
  (reitit/router
   [["/" :home]
    ["/documents" :documents]]))

(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))

(defn mount-root []
  (d/render [#'searchbar] (.getElementById js/document "searchbar"))
  (d/render [#'sidebar] (.getElementById js/document "sidenav"))
  (d/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
