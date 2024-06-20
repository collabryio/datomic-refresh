(ns electric-starter-app.main
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui4]
            [hyperfiddle.router :as router]
            electric-starter-app.v2
            #?(:clj [datomic.client.api :as d])))


(e/def conn)
(e/def db)
;; Saving this file will automatically recompile and update in your browser

(def !state (atom {:create {:value1 ""
                            :value2 ""}
                   :update {:value1 ""
                            :value2 ""}}))

(e/defn Table []
  (e/server
    (binding [conn @(requiring-resolve 'dev/datomic-conn)]
      (binding [db (d/db conn)]
        (e/client
          (dom/div
            (dom/table (dom/props {:class "table  table-light table-hover mt-5"})
                       (dom/thead
                         (dom/tr
                           (dom/th (dom/props {:scope "col"}) (dom/text "Value1"))
                           (dom/th (dom/props {:scope "col"}) (dom/text "Value2"))))
                       (dom/tbody
                         (e/for [value (e/server (d/q '[:find (pull ?e [*])
                                                        :where [?e :test/value1 _]] db))]
                                (println (->> value
                                              (map (fn [inner-vector]
                                                     (filter #(not= (:db/id %) (:db/id (first value))) inner-vector)))
                                              (filter seq)))
                                (dom/tr
                                  (dom/td (dom/text (:test/value1 (first value))))
                                  (dom/td (dom/text (:test/value2 (first value))))
                                  (dom/td
                                    (dom/button
                                      (dom/on "click" (e/fn [event] (router/Navigate!. ['.. [::update (:db/id (first value))]])))
                                      (dom/props {:class            "btn btn-warning text-center ",
                                                  :type             "button"})
                                      (dom/text "Update")))
                                  (dom/td
                                    (dom/button
                                      (dom/on "click" (e/fn [event] (router/Navigate!. ['.. [::delete (:db/id (first value))]])))

                                      (dom/props {:class            "btn btn-danger text-center ",
                                                  :type             "button"})
                                      (dom/text "Delete")))))))
            (dom/button
              (dom/on "click"
                      (e/fn [event] (router/Navigate!. ['.. [::create]])))
              (dom/props {:class "btn btn-success text-center ",
                          :type  "button"})
              (dom/text "Create"))))))))

(e/defn Create []
  (e/server
    (binding [conn @(requiring-resolve 'dev/datomic-conn)]
      (e/client
        (let [state (:create (e/watch !state))]
          (dom/text state)
          (dom/div (dom/props {:class "container",
                               :style {:max-width "400px",
                                       :margin "auto"}})
                   (dom/div (dom/props {:class "input-group flex-nowrap mt-3"})
                            (dom/label (dom/text "Value1:"))
                            (ui4/input (:value1 state) (e/fn [value] (swap! !state assoc-in [:create :value1] value))))
                   (dom/div (dom/props {:class "input-group flex-nowrap mt-3"})
                            (dom/label (dom/text "Value2:"))
                            (ui4/input (:value2 state) (e/fn [value] (swap! !state assoc-in [:create :value2] value))))
                   (dom/div (dom/props {:class "input-group flex-nowrap mt-3"})
                            (dom/button
                              (dom/on "click"
                                      (e/fn [event]
                                            (e/server (d/transact conn {:tx-data [{:test/value1 (:value1 state)
                                                                                   :test/value2 (:value2 state)}]}))
                                            (router/Navigate!. ['.. [::table]])))
                              (dom/props {:class "btn btn-success text-center ",
                                          :type  "button"})
                              (dom/text "Create")))))))))




(e/defn Update [x]
  (e/server
    (binding [conn @(requiring-resolve 'dev/datomic-conn)]
      (binding [db (d/db conn)]
        (e/client
          (let [state (:update (e/watch !state))]
            (dom/text (e/server (d/q '[:find (pull ?e [*])
                                       :in $ ?e
                                       :where [?e :test/value1 _]] db x)))
            (dom/div (dom/props {:class "container",
                                 :style {:max-width "400px",
                                         :margin "auto"}})
                     (dom/div (dom/props {:class "input-group flex-nowrap mt-3"})
                              (dom/label (dom/text "Value1:"))
                              (ui4/input (:value1 state) (e/fn [value] (swap! !state assoc-in [:update :value1] value))))
                     (dom/div (dom/props {:class "input-group flex-nowrap mt-3"})
                              (dom/label (dom/text "Value2:"))
                              (ui4/input (:value2 state) (e/fn [value] (swap! !state assoc-in [:update :value2] value))))
                     (dom/div (dom/props {:class "input-group flex-nowrap mt-3"})
                              (dom/button
                                (dom/on "click"
                                        (e/fn [event]
                                              (e/server (d/transact conn {:tx-data [{:db/id x
                                                                                     :test/value1 (:value1 state)
                                                                                     :test/value2 (:value2 state)}]}))
                                              (router/Navigate!. ['.. [::table]])))
                                (dom/props {:class "btn btn-warning text-center ",
                                            :type  "button"})
                                (dom/text "Update"))))))))))

(e/defn Delete [x]
  (e/server
    (binding [conn @(requiring-resolve 'dev/datomic-conn)]
      (e/client
        (if (js/confirm "Are you sure?")
          (do
            (e/server (d/transact conn {:tx-data [[:db/retractEntity x]]}))
            (router/Navigate!. ['.. [::table]])))))))

(e/defn Main [ring-request]
  (e/client
    (binding [dom/node js/document.body]
      (dom/h1 (dom/text "Hello from Electric Clojure"))
      (dom/div
        (router/link ['.. [::table]] (dom/text "Table")) (dom/text " ")
        (router/link ['.. [::update]] (dom/text "Update")) (dom/text " "))
      (router/router (router/HTML5-History.)
                     (let [[page x :as route] (ffirst router/route)]
                       (if-not page
                         (router/Navigate!. [[::table]])
                         (router/focus [route]
                                       (case page
                                         ::table (e/server (electric-starter-app.v2/TableV2.))
                                         ::update (e/server (Update. x))
                                         ::create (e/server (Create.))
                                         ::delete (e/server (Delete. x))
                                         (e/client (dom/text "no matching route: " (pr-str page)))))))))))






