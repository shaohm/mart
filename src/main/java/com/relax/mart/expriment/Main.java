/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart.expriment;

import com.relax.mart.Dataset;
import com.relax.mart.BipartiPairwiseRankProblem;
import com.relax.mart.MartLearnerParams;
import com.relax.mart.MartModel;
import com.relax.mart.MartNewtonRaphsonLearner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author haimin.shao
 */
public class Main {
    static String workDir = "D:\\SoftwareData\\rhcygwin64\\home\\haimin.shao\\code\\rankboost_code";
    public static void main(String args[]) throws FileNotFoundException, IOException {
        File fromFile = new File(workDir + "\\w.dat");
        File toFile = new File(workDir + "\\v.dat");
        File to2File = new File(workDir + "\\u.dat");
        Dataset ds = new Dataset();
        ds.load(fromFile);
        
        MartLearnerParams params = new MartLearnerParams();
        params.numCarts = 100;
        params.learningRate = 1;
//        params.cartParams = new CartLearnerParams();
        params.cartParams.maxDepth = 1;
        params.cartParams.maxNumLeaves = 6;
        params.cartParams.minNumInstances = 4;
        
        MartNewtonRaphsonLearner learner = new MartNewtonRaphsonLearner();
        learner.setParams(params);
        BipartiPairwiseRankProblem problem = new BipartiPairwiseRankProblem();
        problem.readableLossTopN = 1;
        MartModel model = learner.learn(ds, null, problem);
        model.dump(toFile);
//        System.out.println(model.toString());
        MartModel model2 = new MartModel();
        model2.load(toFile);
        model2.dump(to2File);
    }
}
