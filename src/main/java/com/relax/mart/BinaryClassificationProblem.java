/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

import com.relax.lib.pcj.DoubleVector;
import com.relax.lib.pcj.IntVector;

/**
 * target is +1 for positive example, -1 for negative example
 * @author haimin.shao
 */
public class BinaryClassificationProblem implements Problem {
    double regularizationWeight = 0.0001;
    double positiveExampleWeight = 1.0;
    double negativeExampleWeight = 1.0;
    
    @Override
    public double computeSessionLoss(DoubleVector targets, DoubleVector predicts, Session session) {
        double loss = .0;
        for(int i = 0; i < targets.size(); i++) {
            double t = targets.get(i);
            double p = predicts.get(i);
            double w = (t > 0) ? positiveExampleWeight : negativeExampleWeight;
            loss += w * Math.log(1 + Math.exp(- p * t));
            loss += regularizationWeight * 0.5 * p * p;
        }
        return loss;
    }

    @Override
    public void computeSessionLossGradients(DoubleVector targets, DoubleVector predicts, DoubleVector gradient, DoubleVector secondGradient, Session session) { 
        gradient.clear(0);
        gradient.append(0.0, targets.size());
        secondGradient.clear(0);
        secondGradient.append(0.0, targets.size());

        for(int i = 0; i < targets.size(); i++) {
            double t = targets.get(i);
            double p = predicts.get(i);
            double w = (t > 0) ? positiveExampleWeight : negativeExampleWeight;
            double exp = Math.exp(- t * p);
            gradient.set(w * -t * exp / (1 + exp) + regularizationWeight * p, i);
            secondGradient.set(w * exp / (1 + exp) / (1 + exp) + regularizationWeight, i);
        }
    }

    /** 简单计算精度吧。*/
    @Override
    public double computeReadableSessionLoss(DoubleVector targets, DoubleVector predicts, Session session) {
        double loss = .0;
        for(int i = 0; i < targets.size(); i++) {
            double t = targets.get(i);
            double p = predicts.get(i);
            if(p == 0)
                loss += 0.5;
            if(t < 0 && p > 0 || t > 0 && p < 0)
                loss += 1;
        }
        loss /= targets.size();
        return loss;
    }
    
}
