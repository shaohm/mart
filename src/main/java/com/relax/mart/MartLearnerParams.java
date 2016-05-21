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
public class MartLearnerParams {
    public CartLearnerParams cartParams = new CartLearnerParams();
    public int numCarts = 100;
    public double learningRate = 0.3;
}
