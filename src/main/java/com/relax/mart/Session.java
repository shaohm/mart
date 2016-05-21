/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

import com.relax.lib.pcj.DoubleVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author haimin.shao
 */
public class Session {
    public int offset;
    public String id;
    public List<Instance> instances;
    public DoubleVector targets;
    
    public Session(int offset, String id) {
        this.offset = offset;
        this.id = id;
        this.instances = new ArrayList<Instance>();
        this.targets = new DoubleVector();
    }
    
    public void addExample(Instance instance, double target) {
        this.instances.add(instance);
        this.targets.append(target);
    }
    
    public void orderByTargetDesc() {
        qsort(this.targets, 0, this.targets.size(), this.instances);
        for(int i = 0; i < this.instances.size(); i++) {
            this.instances.get(i).offset = i;
        }
    }
    
    public static void main(String args[]) {
        double value[] = {5,3,1,1,7,8,4,9};
        DoubleVector targets = new DoubleVector(value);
        List<String> instances = Arrays.asList("e", "c", "a", "b", "f", "g", "d", "h");
        int from = 0, to = value.length;
        qsort(targets, from, to, instances);
        System.out.println(targets);
        System.out.println(instances);
    }
    
    private static <T> void qsort(DoubleVector targets, int from, int to, List<T> instances) {
        if(from >= to) return;
        double pivot = targets.get(from);
        T pivotInstance = instances.get(from);
        int i = from, j = to - 1;
        while(true) {
            while(i < j && targets.get(j) <= pivot) j--;
            if(i == j) break;
            targets.set(targets.get(j), i);
            instances.set(i, instances.get(j));
            i++;
            
            while(i < j && targets.get(i) >= pivot) i++;
            if(i == j) break;
            targets.set(targets.get(i), j);
            instances.set(j, instances.get(i));
            j --;
        }
        targets.set(pivot, i);
        instances.set(i, pivotInstance);
        qsort(targets, from, i, instances);
        qsort(targets, i+1, to, instances);
    }
}
