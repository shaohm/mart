/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

/**
 *
 * @author haimin.shao
 */
public class CartLearnerParams {
    public int maxNumLeaves = 6;
    public int maxDepth = 3;
    public int minNumInstances = 6;
    public double minRatioSplitGain = 0.001;
}
