(ns blackbox-cljs.core
  (:require [clojure.set :as set]
            [reagent.core :as r]
            [reagent.dom :as reagent-dom]))

(set! *warn-on-infer* true)

(defonce state* (r/atom {:guesses #{}
                         :hits #{}
                         :entries {}
                         :exits {}
                         :reflections #{}
                         :balls (->> #(vector (inc (rand-int 8))
                                              (inc (rand-int 8)))
                                     (repeatedly)
                                     (distinct)
                                     (take 4)
                                     (set))}))

(defn- in-board?
  [x y]
  (and (<= 1 x 8)
       (<= 1 y 8)))

(defn- on-hover
  [x y]
  (when-not (:done @state*)
    (if-not (#{[0 0] [0 9] [9 0] [9 9]} [x y])
      (swap! state* assoc :hover [x y])
      (swap! state* dissoc :hover))))

(defn- romp
  [depth x y dx dy]
  (let [new-x (+ x dx)
        new-y (+ y dy)]
    (cond ((:balls @state*) [new-x new-y]) [:hit]
          (and (not= 0 depth)
               (or (#{0 9} x)
                   (#{0 9} y))) [:exit x y]
          ((:balls @state*) [(+ new-x dy) (+ new-y dx)]) (romp (inc depth) x y (- dy) (- dx))
          ((:balls @state*) [(- new-x dy) (- new-y dx)]) (romp (inc depth) x y dy dx)
          :else (romp (inc depth) new-x new-y dx dy))))

(defn- on-click
  [x y]
  (when-not (:done @state*)
    (if (in-board? x y)
      (if ((:guesses @state*) [x y])
        (swap! state* update :guesses disj [x y])
        (swap! state* update :guesses conj [x y]))
      (when-not (or (#{[0 0] [0 9] [9 0] [9 9]} [x y])
                    ((:hits @state*) [x y])
                    ((:reflections @state*) [x y])
                    ((:entries @state*) [x y])
                    ((:exits @state*) [x y]))
        (let [[t x2 y2] (romp 0 x y
                              (condp = x 0 1 9 -1 0)
                              (condp = y 0 1 9 -1 0))]
          (condp = t
            :hit (swap! state* update :hits conj [x y])
            :exit (if (= [x y] [x2 y2])
                    (swap! state* update :reflections conj [x y])
                    (let [n (count (:exits @state*))]
                      (swap! state* update :entries assoc [x y] (inc n))
                      (swap! state* update :exits assoc [x2 y2] (inc n))))))))))

(defn- observe
  [e]
  (swap! state* assoc :done true)
  (swap! state* dissoc :hover))

(defn- score
  []
  (+ (count (:entries @state*))
     (count (:exits @state*))
     (count (:hits @state*))
     (count (:reflections @state*))
     (* 5 (count (set/difference (:guesses @state*)
                                 (:balls @state*))))))

(defn- board
  []
  (fn []
    [:div.board-container
     (let [size 20]
       (into [:div.board {:style {:width (* 10 size)
                                  :height (* 10 size)}
                          :on-mouse-leave #(swap! state* dissoc :hover)}]
             (for [y (range 10)
                   x (range 10)]
               [:svg {:width size :height size
                      :style {:position :absolute :left (* size x) :top (* size y)}
                      :on-mouse-enter #(on-hover x y)
                      :on-click #(on-click x y)}
                (when (= [x y] (:hover @state*))
                  [:rect {:width size :height size :fill :#888}])
                (when (and (:done @state*)
                           ((:balls @state*) [x y]))
                  [:circle {:cx (/ size 2) :cy (/ size 2)
                            :r (/ size 3) :fill :#292 :stroke :none}])
                (if (in-board? x y)
                  (if ((:guesses @state*) [x y])
                    [:circle {:cx (/ size 2) :cy (/ size 2)
                              :r (/ size 3) :fill :none
                              :stroke-width 1 :stroke :#f8f8f8}]
                    [:line {:x1 6 :y1 (/ size 2)
                            :x2 (- size 6) :y2 (/ size 2)
                            :stroke-width 1 :stroke :#f8f8f8}])
                  [:g {:stroke-width 0 :fill :#f8f8f8
                       :text-anchor :middle :dominant-baseline :central
                       :transform (str "translate("(/ size 2)","(/ size 2)")")}
                   (cond ((:hits @state*) [x y]) [:text "H"]
                         ((:reflections @state*) [x y]) [:text "R"]
                         ((:entries @state*) [x y]) [:text ((:entries @state*) [x y])]
                         ((:exits @state*) [x y]) [:text ((:exits @state*) [x y])])])])))
     [:div.desc "There are 4 balls in the box."]
     (when (and (= 4 (count (:guesses @state*)))
                (not (:done @state*)))
       [:div.desc [:button {:on-click observe} "OBSERVE"]])
     (when (:done @state*) [:div.desc "Score: " (score)])]))

(defn- page
  []
  (fn []
    [:div.page (when (:hover @state*) {:style {:cursor :pointer}})
     [board]]))

(reagent-dom/render [page] (js/document.getElementById "app"))
