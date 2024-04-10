(ns dev.russell.bts-picker.rankings.batter
  (:require [dev.russell.batboy.people.core :as people]))

(defn- player-batting-stats
  [player-id]
  (future
    {:player-id player-id
     :raw-stats (->> @(people/get-person-stats {:path-params {:id player-id}
                                                :query-params {:stats "season,career,yearByYear"
                                                               :group "hitting"}})
                     :body
                     :stats)}))

(defn- season-hits
  [stats]
  (or (:hits stats) 0))

(defn- season-hit-percentage
  [stats]
  ;; We care more about PA than AB for BTS
  (if (or (nil? (:plateAppearances stats)) (<= (:plateAppearances stats) 0))
    0.0
    (double (/ (:hits stats) (:plateAppearances stats)))))

(defn- range-normalized-score
  [score min max]
  (if (or (nil? score) (nil? min) (nil? max) (= (- max min) 0))
    0.0
    (double (/ (- score min) (- max min)))))

(defn- weight
  [key score stat-totals]
  (condp = key
    :season-hits (* (range-normalized-score score (:season-hits-min stat-totals) (:season-hits-max stat-totals)) (/ 3.0 7.0))
    :season-hit-percentage (* score (/ 4.0 7.0))
    nil))

(defn- apply-weights
  [scores stat-totals]
  (reduce-kv
   (fn [m k v]
     (let [w (weight k v stat-totals)]
       (if w
         (assoc m k w)
         m)))
   {}
   scores))

(defn- sum-score
  [scores]
  (reduce + 0 scores))

(defn- player-score
  [player-id raw-scores stat-totals]
  (let [weighted-scores (apply-weights raw-scores stat-totals)]
    {:player-id player-id
     :score-metadata {:raw-scores raw-scores
                      :weighted-scores weighted-scores}
     :score (sum-score (vals weighted-scores))}))

(defn- score-batter
  [player-stats stat-totals]
  (future
    (player-score (:player-id player-stats)
                  (:raw-scores player-stats)
                  stat-totals)))

(defn- raw-batter-score
  [player-stats]
  (future
    (let [player-id (:player-id @player-stats)
          season-stats (->> (:raw-stats @player-stats) (filter #(= (:displayName (:type %)) "season")) first :splits first :stat)]
      {:player-id player-id
       :raw-scores {:season-hits (season-hits season-stats)
                    :season-hit-percentage (season-hit-percentage season-stats)}})))

(defn- calc-stat-total
  [fn stat-total raw-stat]
  (if (nil? stat-total)
    raw-stat
    (if (nil? raw-stat)
      stat-total
      (fn stat-total raw-stat))))

(defn- stat-totals
  [])

(defn score-batters
  [player-ids]
  (let [raw-with-totals
        (->>
          player-ids
          (pmap player-batting-stats)
          (pmap raw-batter-score)
          (map deref) ; TODO realizes futures for now just to make the totaling easier :shrug:
          (reduce (fn [acc raw-scores]
                    (let [stat-totals {:season-hits-min (calc-stat-total min (-> acc :stat-totals :season-hits-min) (-> raw-scores :raw-scores :season-hits))
                                       :season-hits-max (calc-stat-total max (-> acc :stat-totals :season-hits-max) (-> raw-scores :raw-scores :season-hits))
                                       :season-hit-percentage-min (calc-stat-total min (-> acc :stat-totals :season-hit-percentage-min) (-> raw-scores :raw-scores :season-hit-percentage))
                                       :season-hit-percentage-max (calc-stat-total max (-> acc :stat-totals :season-hit-percentage-max) (-> raw-scores :raw-scores :season-hit-percentage))}]
                      {:stat-totals stat-totals
                       :batter-stats (conj (:batter-stats acc) raw-scores)}))
                  {:stat-totals {:season-hits-min 9999
                                 :season-hits-max 0
                                 :season-hit-percentage-min 1
                                 :season-hit-percentage-max 0}
                   :batter-stats []}))]
   (map (fn [batter-stats] (score-batter batter-stats (:stat-totals raw-with-totals))) (:batter-stats raw-with-totals))))
