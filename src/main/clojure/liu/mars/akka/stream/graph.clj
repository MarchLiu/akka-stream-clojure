(ns liu.mars.akka.stream.graph
  (:import (akka.stream ClosedShape Inlet SourceShape SinkShape FlowShape UniformFanInShape UniformFanOutShape Outlet Graph)
           (akka.stream.javadsl Broadcast Merge)
           (akka.stream.javadsl Source GraphDSL$Builder$ForwardOps GraphDSL$Builder$ReverseOps Sink Flow GraphDSL RunnableGraph Zip)
           (akka.japi.function Function Function2 Function3 Function4 Function5 Function6 Function7 Function8 Function9 Function10 Function11 Function12 Function13 Function14 Function15 Function16 Function17 Function18 Function19)
           (java.util List)))

(defn broadcast
  ([builder n eager]
   (->> (Broadcast/create ^Integer n ^Boolean eager)
        (.add builder)))
  ([builder n]
   (broadcast builder n false)))

(defn merge
  ([builder n eager]
   (->> (Merge/create ^Integer n ^Boolean eager)
        (.add builder)))
  ([builder n]
   (merge builder n false)))

(defn zip [builder]
  (->> (Zip/create)
       (.add builder)))

(defn via [builder x y]
  (cond
    (instance? FlowShape y) (.via x y)
    (instance? UniformFanInShape y) (.viaFanIn x y)
    (instance? UniformFanOutShape y) (.viaFanOut x y)
    :else (->> (.add builder y)
               (.via x))))

(defn |=> [builder node & nodes]
  (let [head (cond
               (instance? Outlet node) (.from builder node)
               (instance? SourceShape node) (.from builder node)
               (instance? FlowShape node) (.from builder node)
               (instance? UniformFanInShape node) (.from builder node)
               (instance? UniformFanOutShape node) (.from builder node)
               (instance? GraphDSL$Builder$ForwardOps node) node
               :else (->> (.add builder node)
                          (.from builder)))]
    (loop [from head ns nodes]
      (let [to (first ns)]
        (if (= 1 (count ns))
          (cond
            (instance? Inlet to) (.toInlet from to)
            (instance? SinkShape to) (.to from to)
            (instance? UniformFanInShape to) (.toFanIn from to)
            (instance? UniformFanOutShape to) (.toFanOut from to))
          (recur (via builder from to) (rest ns)))))))

(defn <=| [builder node & nodes]
  (let [head (cond
               (instance? Outlet node) (.to builder node)
               (instance? SourceShape node) (.to builder node)
               (instance? FlowShape node) (.to builder node)
               (instance? UniformFanInShape node) (.to builder node)
               (instance? UniformFanOutShape node) (.to builder node)
               (instance? GraphDSL$Builder$ReverseOps node) node
               :else (->> (.add builder node)
                          (.to builder)))]
    (loop [to head ns nodes]
      (let [from (first ns)]
        (if (= 1 (count ns))
          (cond
            (instance? Outlet from) (.fromOutlet to from)
            (instance? SourceShape from) (.from to from)
            (instance? UniformFanInShape from) (.fromFanIn to from)
            (instance? UniformFanOutShape from) (.fromFanOut to from)))
        (recur (via builder to from) (rest ns))))))

(defn graph
  ([handle]
   (GraphDSL/create (reify Function
                      (apply [this builder]
                        (handle builder)))))
  ([g handle]
   (if (seq? g)
     (GraphDSL/create ^List (apply list g) (reify Function2
                                             (apply [this builder s]
                                               (handle builder s))))
     (GraphDSL/create ^Graph g (reify Function2
                                 (apply [this builder s]
                                   (handle builder s))))))
  ([^Graph g1 ^Graph g2 mat handle]
   (GraphDSL/create g1 g2 mat (reify Function3
                                (apply [this builder s1 s2]
                                  (handle builder s1 s2)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 mat handle]
   (GraphDSL/create3 g1 g2 g3
                     (reify Function3
                       (apply [this m1 m2 m3]
                         (mat m1 m2 m3)))
                     (reify Function4
                       (apply [this builder s1 s2 s3]
                         (handle builder s1 s2 s3)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 mat handle]
   (GraphDSL/create4 g1 g2 g3 g4
                     (reify Function4
                       (apply [this m1 m2 m3 m4]
                         (mat m1 m2 m3 m4)))
                     (reify Function5
                       (apply [this builder s1 s2 s3 s4]
                         (handle builder s1 s2 s3 s4)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 mat handle]
   (GraphDSL/create5 g1 g2 g3 g4 g5
                     (reify Function5
                       (apply [this m1 m2 m3 m4 m5]
                         (mat m1 m2 m3 m4 m5)))
                     (reify Function6
                       (apply [this builder s1 s2 s3 s4 s5]
                         (handle builder s1 s2 s3 s4 s5)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 mat handle]
   (GraphDSL/create6 g1 g2 g3 g4 g5 g6
                     (reify Function6
                       (apply [this m1 m2 m3 m4 m5 m6]
                         (mat m1 m2 m3 m4 m5 m6)))
                     (reify Function7
                       (apply [this builder s1 s2 s3 s4 s5 s6]
                         (handle builder s1 s2 s3 s4 s5 s6)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 mat handle]
   (GraphDSL/create7 g1 g2 g3 g4 g5 g6 g7
                     (reify Function7
                       (apply [this m1 m2 m3 m4 m5 m6 m7]
                         (mat m1 m2 m3 m4 m5 m6 m7)))
                     (reify Function8
                       (apply [this builder s1 s2 s3 s4 s5 s6 s7]
                         (handle builder s1 s2 s3 s4 s5 s6 s7)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 mat handle]
   (GraphDSL/create8 g1 g2 g3 g4 g5 g6 g7 g8
                     (reify Function8
                       (apply [this m1 m2 m3 m4 m5 m6 m7 m8]
                         (mat m1 m2 m3 m4 m5 m6 m7 m8)))
                     (reify Function9
                       (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8]
                         (handle builder s1 s2 s3 s4 s5 s6 s7 s8)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 mat handle]
   (GraphDSL/create9 g1 g2 g3 g4 g5 g6 g7 g8 g9
                     (reify Function9
                       (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9]
                         (mat m1 m2 m3 m4 m5 m6 m7 m8 m9)))
                     (reify Function10
                       (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9]
                         (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10 mat handle]
   (GraphDSL/create10 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10
                      (reify Function10
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10)))
                      (reify Function11
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10 ^Graph g11
    mat handle]
   (GraphDSL/create11 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11
                      (reify Function11
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11)))
                      (reify Function12
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10
    ^Graph g11 ^Graph g12
    mat handle]
   (GraphDSL/create12 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11 g12
                      (reify Function12
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12)))
                      (reify Function13
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10
    ^Graph g11 ^Graph g12 ^Graph g13
    mat handle]
   (GraphDSL/create13 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11 g12 g13
                      (reify Function13
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13)))
                      (reify Function14
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10
    ^Graph g11 ^Graph g12 ^Graph g13 ^Graph g14
    mat handle]
   (GraphDSL/create14 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11 g12 g13 g14
                      (reify Function14
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14)))
                      (reify Function15
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10
    ^Graph g11 ^Graph g12 ^Graph g13 ^Graph g14 ^Graph g15
    mat handle]
   (GraphDSL/create15 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11 g12 g13 g14 g15
                      (reify Function15
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15)))
                      (reify Function16
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10
    ^Graph g11 ^Graph g12 ^Graph g13 ^Graph g14 ^Graph g15 ^Graph g16
    mat handle]
   (GraphDSL/create16 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11 g12 g13 g14 g15 g16
                      (reify Function16
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15 m16]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15 m16)))
                      (reify Function17
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15 s16]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15 s16)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10
    ^Graph g11 ^Graph g12 ^Graph g13 ^Graph g14 ^Graph g15 ^Graph g16 ^Graph g17
    mat handle]
   (GraphDSL/create17 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11 g12 g13 g14 g15 g16 g17
                      (reify Function17
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15 m16 m17]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15 m16 m17)))
                      (reify Function18
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15 s16 s17]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15 s16 s17)))))
  ([^Graph g1 ^Graph g2 ^Graph g3 ^Graph g4 ^Graph g5 ^Graph g6 ^Graph g7 ^Graph g8 ^Graph g9 ^Graph g10
    ^Graph g11 ^Graph g12 ^Graph g13 ^Graph g14 ^Graph g15 ^Graph g16 ^Graph g17 ^Graph g18
    mat handle]
   (GraphDSL/create18 g1 g2 g3 g4 g5 g6 g7 g8 g9 g10 g11 g12 g13 g14 g15 g16 g17 g18
                      (reify Function18
                        (apply [this m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15 m16 m17 m18]
                          (mat m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15 m16 m17 m18)))
                      (reify Function19
                        (apply [this builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15 s16 s17 s18]
                          (handle builder s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15 s16 s17 s18))))))

(defn runnable-graph
  [& args]
  (RunnableGraph/fromGraph (apply graph args)))

(defn source-graph
  [& args]
  (Source/fromGraph (apply graph args)))

(defn flow-graph
  [& args]
  (Flow/fromGraph (apply graph args)))

(defn sink-graph
  [& args]
  (Sink/fromGraph (apply graph args)))

(defn closed-shape []
  (ClosedShape/getInstance))