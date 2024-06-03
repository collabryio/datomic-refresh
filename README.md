# Electric Starter App

Based on: https://github.com/hyperfiddle/electric-starter-app

## Instructions

Dev build:

* Shell: `clj -A:dev -X dev/-main`, or repl: `(dev/-main)`
* http://localhost:8080
* Electric root function: [src/electric_starter_app/main.cljc](src/electric_starter_app/main.cljc)
* Hot code reloading works: edit -> save -> see app reload in browser

## Differences from: hyperfiddle/electric-starter-app 

* Deleted all files that are not required for starter app: src-dev, src-build etc.