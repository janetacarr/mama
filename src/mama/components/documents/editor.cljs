(ns mama.components.documents.editor
  (:require [reagent.core :as r]
            [ajax.core :refer [POST PUT]]
            [clojure.string :as string]
            [cognitect.transit :as transit]
            [goog.string :as gstring]
            [goog.string.format]))

(defn document-editor []
  (r/with-let [document-state (r/atom {})
               writer (transit/writer :json-verbose)]
    (do
      (POST "http://localhost:8080/documents"
            {:body (transit/write writer {"org_id" 1
                                          "name" ""
                                          "title" ""
                                          "body" ""})
             :handler (fn [req]
                        (swap! document-state assoc :doc_id (get req "document_id")))})
      (js/setInterval #(let [doc_id (:doc_id @document-state)
                             uri "http://localhost:8080/documents"
                             title (:title @document-state)
                             body (:body @document-state)
                             name (-> title
                                      (string/split #" ")
                                      (string/join)
                                      (string/lower-case))]
                         (PUT uri {:body (transit/write writer {"org_id" 1
                                                                "doc_id" doc_id
                                                                "title" title
                                                                "body" body
                                                                "name" name})})) 15000)
      (js/setInterval #(let [title (.-value
                                    (.getElementById js/document "title"))
                             body (.-value
                                   (.getElementById js/document "body"))]
                         (swap! document-state assoc :title title :body body)) 3000)
      [:section.editor-section
       [:textarea#title.title {:placeholder "Title" :autoFocus true}]
       [:hr {:style {:border :none}}]
       [:textarea#body.editor {:placeholder "Body"} ]])))
