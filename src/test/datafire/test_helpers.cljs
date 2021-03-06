(ns datafire.test-helpers
  (:require [cljs.core.async :refer [go]]
            [datascript.core :as d]
            [datafire.core :as df]
            ["firebase/app" :as firebase]
            ["firebase/firestore"]))

(def firebase-config #js {:apiKey "AIzaSyAYJX2_LdpTbdgcaGYvSbfz9hJplqTPi7Y"
                          :authDomain "datafire.firebasedatafire.com"
                          :projectId "datafire"})
(def emulator-settings #js {:host "localhost:8080" :ssl false})

(def default-test-app (str df/default-firebase-app "-TEST"))

(defn test-link
  ([] (test-link {}))
  ([{:keys [schema path name granularity]
     :or {schema {}
          path (str "tmp/rand-path-" (rand))
          name default-test-app
          granularity :tx}}]
   (go (let [_ (try (.app firebase name)
                    (catch js/Error _
                      (let [new-app (.initializeApp firebase firebase-config name)
                            _ (.settings (.firestore new-app) emulator-settings)]
                        new-app)))
             conn (d/create-conn schema)
             link (df/create-link conn path {:name name :granularity granularity})]
         (df/listen! link)
         [conn link path name]))))

(defn query-lethal-weapon [conn]
  (d/q '[:find ?e .
         :where [?e :movie/title "Lethal Weapon"]]
       @conn))

(defn pull-lethal-weapon [conn]
  (d/pull @conn '[*] (query-lethal-weapon conn)))

(def pulled-lethal-weapon-snapshot
  {:db/id 57
   :movie/cast
   [{:db/id 13, :person/born "1956-01-03", :person/name "Mel Gibson"}
    {:db/id 14
     :person/born "1946-07-22"
     :person/name "Danny Glover"}
    {:db/id 15
     :person/born "1944-07-29"
     :person/name "Gary Busey"}]
   :movie/director
   [{:db/id 12
     :person/born "1930-04-24"
     :person/name "Richard Donner"}]
   :movie/sequel
   {:db/id 58
    :movie/cast
    [{:db/id 13
      :person/born "1956-01-03"
      :person/name "Mel Gibson"}
     {:db/id 14
      :person/born "1946-07-22"
      :person/name "Danny Glover"}
     {:db/id 37
      :person/born "1943-02-09"
      :person/name "Joe Pesci"}]
    :movie/director
    [{:db/id 12
      :person/born "1930-04-24"
      :person/name "Richard Donner"}]
    :movie/sequel
    {:db/id 64
     :movie/cast
     [{:db/id 13
       :person/born "1956-01-03"
       :person/name "Mel Gibson"}
      {:db/id 14
       :person/born "1946-07-22"
       :person/name "Danny Glover"}
      {:db/id 37
       :person/born "1943-02-09"
       :person/name "Joe Pesci"}]
     :movie/director
     [{:db/id 12
       :person/born "1930-04-24"
       :person/name "Richard Donner"}]
     :movie/title "Lethal Weapon 3"
     :movie/year 1992}
    :movie/title "Lethal Weapon 2"
    :movie/year 1989}
   :movie/title "Lethal Weapon"
   :movie/year 1987})