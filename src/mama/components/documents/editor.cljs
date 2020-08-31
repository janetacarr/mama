(ns mama.components.documents.editor)

(defn document-editor []
  (do
    (js/setInterval #(js/console.log "happening every few seconds") 15000)
    [:section.editor-section
     [:textarea.title {:placeholder "Title" :autofocus "true"}]
     [:hr {:style {:border :none}}]
     [:textarea.editor {:placeholder "Body" :contenteditable "true"} ]]))
