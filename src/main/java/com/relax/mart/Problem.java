/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;
import com.relax.lib.pcj.DoubleVector;

/**
 * 对于list-wise和pair-wise，每个函数处理一个session。对于point-wise，每个函数处理一个数据集。
 * @author haimin.shao
 */
public interface Problem {
    
    /** compute total loss for all predict */
    double computeSessionLoss(DoubleVector targets, DoubleVector predicts, Session session);
    
    /** the gradient of loss as a function of all predicts */
    void computeSessionLossGradients(DoubleVector targets, DoubleVector predicts, DoubleVector gradient, DoubleVector secondGradient, Session session);
    
    /** compute total loss for all predict */
    double computeReadableSessionLoss(DoubleVector targets, DoubleVector predicts, Session session);
}
