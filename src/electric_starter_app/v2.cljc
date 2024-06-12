(ns electric-starter-app.v2
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui4]
            [hyperfiddle.router :as router]
            #?(:clj [datomic.client.api :as d])))


(e/def conn)
(e/def db)
(def !state (atom nil))
(def !refresh (atom {:refresh false}))

(defn filter-by-db-id [data id]
  (->> data
       (map (fn [inner-vector]
              (filter #(not= (:db/id %) id) inner-vector)))
       (filter seq)))
(e/defn Table []
  (e/server
    (binding [conn @(requiring-resolve 'dev/datomic-conn)]
      (binding [db (d/db conn)]
        (e/client
          (let [refresh (e/watch !refresh)]
           (let [state (e/watch !state)]
             (if (:refresh refresh)
               (do (reset! !state (e/server (d/q '[:find (pull ?e [*])
                                                :where [?e :test/value1 _]] db)))
                   (reset! !refresh {:refresh false})))
             (reset! !state (e/server (d/q '[:find (pull ?e [*])
                                             :where [?e :test/value1 _]] db)))
             (dom/text (e/server (d/q '[:find (pull ?e [*])
                                        :where [?e :test/value1 _]] db)))
             (dom/div
               (dom/table (dom/props {:class "table  table-light table-hover mt-5"})
                          (dom/thead
                            (dom/tr
                              (dom/th (dom/props {:scope "col"}) (dom/text "Value1"))
                              (dom/th (dom/props {:scope "col"}) (dom/text "Value2"))))
                          (dom/tbody
                            (e/for [value state]
                                   (println value)
                                   (dom/tr
                                     (dom/td (dom/text (:test/value1 (first value))))
                                     (dom/td (dom/text (:test/value2 (first value))))
                                     (dom/td
                                       (dom/button
                                         (dom/on "click" (e/fn [event] (router/Navigate!. ['.. [:electric-starter-app.main/update (:db/id (first value))]])))
                                         (dom/props {:class "btn btn-warning text-center ",
                                                     :type  "button"})
                                         (dom/text "Update")))
                                     (dom/td
                                       (dom/button
                                         (dom/on "click" (e/fn [event] (e/server (d/transact conn {:tx-data [[:db/retractEntity (:db/id (first value))]]}))))

                                         (dom/props {:class "btn btn-danger text-center ",
                                                     :type  "button"})
                                         (dom/text "Delete")))))))
               (dom/button
                 (dom/on "click"
                         (e/fn [event] (router/Navigate!. ['.. [:electric-starter-app.main/create]])))
                 (dom/props {:class "btn btn-success text-center ",
                             :type  "button"})
                 (dom/text "Create"))))))))))

(e/defn TableV2 []
  (e/client
    (let [state (e/watch !state)
          refresh (e/watch !refresh)]
      (e/server
        (binding [conn @(requiring-resolve 'dev/datomic-conn)]
          (binding [db (d/db conn)]
            (e/client
              (dom/text state)
              (reset! !state (e/server (d/q '[:find (pull ?e [*])
                                             :where [?e :test/value1 _]] db)))
              (dom/div
                (dom/table (dom/props {:class "table  table-light table-hover mt-5"})
                           (dom/thead
                             (dom/tr
                               (dom/th (dom/props {:scope "col"}) (dom/text "Value1"))
                               (dom/th (dom/props {:scope "col"}) (dom/text "Value2"))))
                           (dom/tbody
                             (e/for [value state]
                                    (println value)
                                    (dom/tr
                                      (dom/td (dom/text (:test/value1 (first value))))
                                      (dom/td (dom/text (:test/value2 (first value))))
                                      (dom/td
                                        (dom/button
                                          (dom/on "click" (e/fn [event] (router/Navigate!. ['.. [:electric-starter-app.main/update (:db/id (first value))]])))
                                          (dom/props {:class "btn btn-warning text-center ",
                                                      :type  "button"})
                                          (dom/text "Update")))
                                      (dom/td
                                        (dom/button
                                          (dom/on "click" (e/fn [event] (e/server (d/transact conn {:tx-data [[:db/retractEntity (:db/id (first value))]]}))
                                                                (reset! !state (filter-by-db-id state (:db/id (first value))))))

                                          (dom/props {:class "btn btn-danger text-center ",
                                                      :type  "button"})
                                          (dom/text "Delete")))))))
                (dom/button
                  (dom/on "click"
                          (e/fn [event] (router/Navigate!. ['.. [:electric-starter-app.main/create]])))
                  (dom/props {:class "btn btn-success text-center ",
                              :type  "button"})
                  (dom/text "Create"))))
            ))))
    ))





