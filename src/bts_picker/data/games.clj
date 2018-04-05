(ns bts-picker.data.games
  (:require [bts-picker.mlb-api.game.core :as api-game]
            [bts-picker.mlb-api.schedule.core :as api-schedule]
            [bts-picker.util.date :as date-util]))

(defn game-id
  [data]
  (some-> data :gamePk str))

(defn- transform-schedule-to-game-ids
  [schedule-data]
  (some->> schedule-data :dates first :games (map game-id)))

(defn- transform-game-data-status
  [status]
  {:code (:abstractGameCode status)
   :detail (:detailedState status)})

(defn- transform-game-data-venue
  [venue]
  {:id (some-> venue :id str)
   :name (:name venue)
   :location {:city (some-> venue :location :city)
              :state (some-> venue :location :state)
              :state-abbrev (some-> venue :location :stateAbbrev)}
   :timezone (some-> venue :timeZone)})

(defn- transform-game-data-probable-pitchers
  [probable-pitchers]
  {:home (some-> probable-pitchers :home :id str)
   :away (some-> probable-pitchers :away :id str)})

(defn- transform-game-data-team
  [team]
  (let [team (select-keys team [:id :name])]
    {:id (some-> team :id str)
     :name (some-> team :name)}))

(defn- transform-game-data-teams
  [teams]
  {:home (some-> teams :home transform-game-data-team)
   :away (some-> teams :away transform-game-data-team)})

(defn- transform-game-data-player
  [player]
  {:id (some-> player :id str)
   :name (some-> player :fullName)})

(defn- transform-game-data-players
  [players]
  (map transform-game-data-player (some->> players vals (filter :active))))

(defn- transform-game-data
  [game]
  (let [game-data (:gameData game)]
    {:id (game-id game)
     :date (some-> game-data :datetime :dateTime)
     :status (some-> game-data :status transform-game-data-status)
     :venue (some-> game-data :venue transform-game-data-venue)
     :weather (some-> game-data :weather)
     :probable-pitchers (some-> game-data :probablePitchers transform-game-data-probable-pitchers)
     :teams (some-> game-data :teams transform-game-data-teams)
     :players (some-> game-data :players transform-game-data-players)}))

(defn get-games
  ([]
   (get-games (date-util/now)))
  ([date]
   (transform-schedule-to-game-ids (api-schedule/get-schedule {:date (date-util/format-date date)}))))

(defn get-game
  [game-id]
  (transform-game-data (api-game/get-live-feed game-id)))
