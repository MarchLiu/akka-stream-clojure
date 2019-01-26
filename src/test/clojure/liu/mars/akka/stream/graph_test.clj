(ns liu.mars.akka.stream.graph-test
  (:require [clojure.test :refer :all])
  (:require [jaskell.handle :refer [function def-generator-1 def-generator-2]])
  (:require [liu.mars.akka.stream.graph :as g :refer [|=>]])
  (:import (akka.stream.javadsl Source Sink Flow Keep ZipWith Zip)
           (akka.japi.function Function Procedure Function2)
           (akka.actor ActorSystem)
           (akka.stream ActorMaterializer UniformFanInShape Outlet Inlet SourceShape FlowShape)))

(defmacro procedure [[arg] & body]
  `(reify Procedure
     (apply [_ ~arg]
       ~@body)))

(def-generator-1 predicate [akka.japi.function.Predicate test])
(def-generator-2 function2 [Function2 apply])

(deftest basic-graph
  (let [system (ActorSystem/create "test")
        materializer (ActorMaterializer/create system)
        source (Source/from (range 10))
        sink (Sink/foreach (procedure [param] (is (instance? String param))))
        f1 (-> (Flow/of Integer)
               (.map (function [_ arg]
                       (+ 10 arg))))
        f2 (-> (Flow/of Integer)
               (.map (function [_ arg]
                       (+ 20 arg))))
        f3 (-> (Flow/of Integer)
               (.map (function [_ arg]
                       (str arg))))
        f4 (-> (Flow/of Integer)
               (.map (function [_ arg]
                       (+ 30 arg))))]
    (-> (g/runnable-graph
          sink
          (fn [builder out]
            (let [bcast (g/broadcast builder 2)
                  merge (g/merge builder 2)]
              (|=> builder source f1 bcast f2 merge f3 out)
              (|=> builder bcast f4 merge))
            (g/closed-shape)))
        (.run materializer))))

(deftest parallel-stream
  (let [system (ActorSystem/create "test")
        materializer (ActorMaterializer/create system)
        top-head-sink (Sink/foreach (procedure [param] (is (= 2 param))))
        bottom-head-sink (Sink/foreach (procedure [param] (is (= 2 param))))
        shared-doubler (-> (Flow/of Integer)
                           (.map (function [_ param] (* 2 param))))]
    (-> (g/runnable-graph top-head-sink bottom-head-sink (Keep/both)
                          (fn [builder top bottom]
                            (let [bcast (g/broadcast builder 2)]
                              (|=> builder (Source/single 1) bcast shared-doubler (.in top))
                              (|=> builder bcast shared-doubler (.in bottom)))
                            (g/closed-shape)))
        (.run materializer))))

(deftest prefix-filter
  (let [system (ActorSystem/create "test")
        materializer (ActorMaterializer/create system)
        sinks (for [prefix ["a" "b" "c"]]
                (-> (Flow/of String)
                    (.filter (predicate [_ param] (.startsWith param prefix)))
                    (.toMat (Sink/foreach
                              (procedure [param] (is (.startsWith param prefix))))
                            (Keep/right))))]
    (-> (g/runnable-graph
          sinks
          (fn [builder outs]
            (let [bcast (g/broadcast builder (count outs))]
              (|=> builder (Source/from ["ax", "bx", "cx"]) bcast)
              (doseq [sink outs]
                (|=> builder bcast sink)))
            (g/closed-shape)))
        (.run materializer))))

(deftest partial-graphs
  (let [system (ActorSystem/create "test")
        materializer (ActorMaterializer/create system)
        pick-max-of-three (g/graph (fn [builder]
                                     (let [zip (ZipWith/create (function2 [_ x y] (max x y)))
                                           zip1 (.add builder zip)
                                           zip2 (.add builder zip)]
                                       (|=> builder (.out zip1) (.in0 zip2))
                                       (UniformFanInShape.
                                         ^Outlet (.out zip2)
                                         (into-array Inlet [(.in0 zip1) (.in1 zip1) (.in1 zip2)])))))
        result-sink (Sink/foreach (procedure [param] (is (number? param))))]
    (-> (g/runnable-graph
          result-sink
          (fn [builder sink]
            (let [pm (.add builder pick-max-of-three)]
              (|=> builder (Source/single 1) (.in pm 0))
              (|=> builder (Source/single 2) (.in pm 1))
              (|=> builder (Source/single 3) (.in pm 2))
              (|=> builder (.out pm) (.in sink)))
            (g/closed-shape)))
        (.run materializer))))

(deftest zip-graph
  (let [system (ActorSystem/create "test")
        materializer (ActorMaterializer/create system)
        src (Source/from (range))]
    (-> (g/source-graph
          (fn [builder]
            (let [zip (.add builder (Zip/create))]
              (|=> builder (.filter src (predicate [_ param] (even? param))) (.in0 zip))
              (|=> builder (.filter src (predicate [_ param] (odd? param))) (.in1 zip))
              (SourceShape/of (.out zip)))))
        (.take 100)
        (.runWith (Sink/foreach (procedure [arg] (is (= (inc (.first arg)) (.second arg))))) materializer))))

(deftest flow-graph
  (let [system (ActorSystem/create "test")
        materializer (ActorMaterializer/create system)
        pairs (g/flow-graph
                (fn [builder]
                  (let [bcast (g/broadcast builder 2)
                        zip (.add builder (Zip/create))]
                    (|=> builder bcast (.in0 zip))
                    (|=> builder bcast
                         (-> (Flow/of Integer)
                             (.map (function [_ arg] (str arg))))
                         (.in1 zip))
                    (FlowShape/of (.in bcast) (.out zip)))))]
    (-> (Source/single 1)
        (.via pairs)
        (.runWith (Sink/foreach (procedure [arg] (println arg))) materializer))))