(ns dev
  (:require
   electric-starter-app.main
   [hyperfiddle.electric :as e]
   #?(:clj datomic.client.api)
   #?(:clj electric-starter-app.db-create)
   #?(:clj [electric-starter-app.server-jetty :as jetty])
   #?(:clj [shadow.cljs.devtools.api :as shadow])
   #?(:clj [shadow.cljs.devtools.server :as shadow-server])
   #?(:clj [clojure.tools.logging :as log])))

(comment (-main)) ; repl entrypoint

(def datomic-client)
(def datomic-conn)

#?(:clj ;; Server Entrypoint
   (do
     (def config
       {:host "0.0.0.0"
        :port 8080
        :resources-path "public/electric_starter_app"
        :manifest-path ; contains Electric compiled program's version so client and server stays in sync
        "public//electric_starter_app/js/manifest.edn"})

     (defn -main [& args]
       (log/info "Starting Electric compiler and server...")

       (shadow-server/start!)
       (shadow/watch :dev)
       (comment (shadow-server/stop!))

       (def server (jetty/start-server!
                     (fn [ring-request]
                       (e/boot-server {} electric-starter-app.main/Main ring-request))
                     config))

       (comment (.stop server))
       (alter-var-root #'datomic-client (constantly (datomic.client.api/client {:server-type :dev-local
                                                                                :storage-dir :mem
                                                                                :system "ci"})))
       (datomic.client.api/create-database datomic-client {:db-name "test"})
       (alter-var-root #'datomic-conn (constantly (datomic.client.api/connect datomic-client {:db-name "test"})))
       (electric-starter-app.db-create/create-schema datomic-conn))))


#?(:cljs ;; Client Entrypoint
   (do
     (def electric-entrypoint (e/boot-client {} electric-starter-app.main/Main nil))

     (defonce reactor nil)

     (defn ^:dev/after-load ^:export start! []
       (set! reactor (electric-entrypoint
                       #(js/console.log "Reactor success:" %)
                       #(js/console.error "Reactor failure:" %))))

     (defn ^:dev/before-load stop! []
       (when reactor (reactor)) ; stop the reactor
       (set! reactor nil))))
