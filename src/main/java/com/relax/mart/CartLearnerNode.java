/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

import com.relax.lib.pcj.DoubleVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author haimin.shao
 */
public class CartLearnerNode extends CartModelNode {
    public List<Instance> instances;
    public DoubleVector targets;
    public int splitLeftCount;
    public int splitRightCount;
    
    /** used in learning process. compute all statistics here.
     * @param instances
     * @param targets
     * @param seq */
    public CartLearnerNode(List<Instance> instances, DoubleVector targets, int seq, CartLearnerParams params) {
        this.instances = instances;
        this.targets = targets;
        this.seq = seq;
        
        // 收集所有的特征
        Set<Integer> featureSet = new TreeSet<Integer>();
        double tSum = .0;
        double tSquaredSum = .0;
        int count = 0;
        List<Pair<Instance, Double>> examples = new ArrayList();
        
        for(int i = 0; i < this.instances.size(); i++) {
            Instance instance = this.instances.get(i);
            double target = this.targets.get(i);
            tSum += target;
            tSquaredSum += target * target;
            count += 1;
            examples.add(new Pair(instance, target));
            for(int j = 0; j < instance.getSize(); j++) {
                int featureIndex = instance.indexAt(j);
                featureSet.add(featureIndex);
            }
        }
        
//        System.out.println("fs:" + featureSet);

        // 找最优分割点
        class ExampleComparator implements Comparator<Pair<Instance, Double>> {
            int feature;
            public ExampleComparator(int feature) {
                this.feature = feature;
            }
            @Override
            public int compare(Pair<Instance, Double> o1, Pair<Instance, Double> o2) {
                double v1 = o1.first.findValue(feature);
                double v2 = o2.first.findValue(feature);
                return Double.compare(v1, v2);
            }
        }
        
        int bestSplitFeature = -1;
        double bestSplitValue = .0, bestSplitEndValue = .0;
        double bestSplitGain = .0;
        int bestSplitLeftCount = 0;
        int bestSplitRightCount = 0;
        double errorTotal = tSquaredSum - tSum * tSum / count;
        for(int feature : featureSet) {
            Collections.sort(examples, new ExampleComparator(feature));
            double tSumLeft = .0, tSumRight = tSum;
            double tSquaredSumLeft = .0, tSquaredSumRight = tSquaredSum;
            int countLeft = 0, countRight = count;
            
//            System.out.println("f:" + feature);
            
            double prevValue = Double.NEGATIVE_INFINITY;
            double psValue = Double.NEGATIVE_INFINITY, ppsValue = Double.NEGATIVE_INFINITY;
            boolean inBestRegion = false;
            for(int i = 0; i < count; i++) {
                Pair<Instance, Double> e = examples.get(i);
                double currValue = e.first.findValue(feature);
                if(i > 0 && prevValue != currValue) {
                    ppsValue = psValue;
                    psValue = prevValue;
                    // record  prevValue
                    double errorLeft = tSquaredSumLeft - tSumLeft * tSumLeft / countLeft;
                    double errorRight = tSquaredSumRight - tSumRight * tSumRight / countRight;
                    double gain = errorTotal - errorLeft - errorRight;
                    if(gain > bestSplitGain) {
                        if(countLeft >= params.minNumInstances && countRight >= params.minNumInstances) {
                            bestSplitFeature = feature;
                            bestSplitValue = bestSplitEndValue = prevValue;
                            bestSplitGain = gain;
                            bestSplitLeftCount = countLeft;
                            bestSplitRightCount = countRight;
                            inBestRegion = true;
                        }
//                        System.out.println(bestSplitFeature);
//                        System.out.println(bestSplitGain);
                    } else if(gain < bestSplitGain) {
                        if(inBestRegion)
                            bestSplitEndValue = psValue;
                        inBestRegion = false;
                    }
//                    System.out.println("i:" + i + " " + currValue + " " + prevValue + " " + gain);
//                    System.out.println("e:" + i + " " + errorTotal + " " + errorLeft + " " + errorRight);
                    
                }
                tSumLeft += e.second;
                tSumRight -= e.second;
                tSquaredSumLeft += e.second * e.second;
                tSquaredSumRight -= e.second * e.second;
                countLeft ++;
                countRight --;
                prevValue = currValue;
            }            
        }
        
        this.numInstances = count;
        this.error = errorTotal;
        this.predict = tSum / count;
        this.splitFeature = bestSplitFeature;
        this.splitValue = bestSplitValue;
//        System.out.println("split start end: " + bestSplitValue + " " + bestSplitEndValue);
//        this.splitValue = (bestSplitValue + bestSplitEndValue) / 2;
        this.splitGain = bestSplitGain;
        this.splitLeftCount = bestSplitLeftCount;
        this.splitRightCount = bestSplitRightCount;
    }
    
    public void split(CartLearnerParams params) {
        if(this.splitFeature < 0)
            throw new RuntimeException("split not found");
        List<Instance> instancesLeft = new ArrayList(), instancesRight = new ArrayList();
        DoubleVector targetsLeft = new DoubleVector(), targetsRight = new DoubleVector();
        for(int i = 0; i < this.instances.size(); i++) {
            Instance instance = this.instances.get(i);
            double target = this.targets.get(i);
            double value = instance.findValue(this.splitFeature);
            if(value <= this.splitValue) {
                instancesLeft.add(instance);
                targetsLeft.append(target);
            } else {
                instancesRight.add(instance);
                targetsRight.append(target);
            }
        }
        this.left = new CartLearnerNode(instancesLeft, targetsLeft, this.seq * 2, params);
        this.left.parent = this;
        this.right = new CartLearnerNode(instancesRight, targetsRight, this.seq * 2 + 1, params);
        this.right.parent = this;
    }
}
