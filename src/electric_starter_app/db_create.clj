(ns electric-starter-app.db-create
  (:require [datomic.client.api :as d]))

(def client (d/client {:server-type :dev-local
                       :storage-dir :mem
                       :system      "ci"}))

(d/create-database client {:db-name "test"})

(def conn (d/connect client {:db-name "test"}))



(defn create-schema [conn]
  (d/transact conn {:tx-data [{:db/ident :test/value1
                               :db/valueType :db.type/string
                               :db/cardinality :db.cardinality/one}
                              {:db/ident :test/value2
                               :db/valueType :db.type/string
                               :db/cardinality :db.cardinality/one}]})
  (d/transact conn {:tx-data [{:test/value1 "test1"
                               :test/value2 "test2"}]}))


