/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

import com.relax.lib.pcj.DoubleVector;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author haimin.shao
 */
public class CartLearner {

    private CartLearnerParams params;

    public CartLearnerNode learn(List<Instance> instances, DoubleVector targets, Problem problem) {
        CartLearnerNode root = new CartLearnerNode(instances, targets, 1, params);
        PriorityQueue<CartLearnerNode> leavesToSplit = new PriorityQueue(16, new Comparator<CartLearnerNode>() {
            @Override
            public int compare(CartLearnerNode o1, CartLearnerNode o2) {
                return Double.compare(o2.splitGain, o1.splitGain);
            }
        });
        leavesToSplit.add(root);
        int numLeaves = 1;
        while (!leavesToSplit.isEmpty()) {
            CartLearnerNode node = leavesToSplit.poll();
            if (Math.log(node.seq)/Math.log(2) >= params.maxDepth 
                    || node.splitLeftCount < params.minNumInstances 
                    || node.splitRightCount < params.minNumInstances) {
                System.out.println("E: " + node.seq + " " + params.maxDepth);
                System.out.println("F: " + node.splitLeftCount + " " + params.minNumInstances);
                System.out.println("G: " + node.splitRightCount + " " + params.minNumInstances);
                continue;
            }
            if(node.error > params.minRatioSplitGain && node.splitGain > root.error * params.minRatioSplitGain) {
                node.split(params);
                leavesToSplit.add((CartLearnerNode)node.left);
                leavesToSplit.add((CartLearnerNode)node.right);
                numLeaves += 1;
                System.out.println("H: " + node.seq + " " + params.maxDepth);
                if (numLeaves >= params.maxNumLeaves) {
                    System.out.println("I: " + node.seq + " " + params.maxDepth);
                    leavesToSplit.clear();
                }
            } else {
                System.out.println("J:" + node.error + " " + node.splitGain + " " + params.minRatioSplitGain);
            }
        }
        return root;
    }

    /**
     * @return the params
     */
    public CartLearnerParams getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(CartLearnerParams params) {
        this.params = params;
    }
}
