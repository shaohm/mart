/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart.expriment;

import com.relax.mart.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 *
 * @author haimin.shao
 */
public class IrisExperiment {
    static String workDir = "D:/NetBeansProjects/mart/src/main/resources";
    
    public static void readyData() throws FileNotFoundException, UnsupportedEncodingException {
        File originFile = new File(workDir + "/iris.data");
        File trainFile = new File(workDir + "/iris_train.data");
        File testFile = new File(workDir + "/iris_test.data");
        Scanner in = new Scanner(originFile, "utf-8");
        PrintWriter trainWriter = new PrintWriter(trainFile, "utf-8");
        PrintWriter testWriter = new PrintWriter(testFile, "utf-8");
        int i = 0;
        while(in.hasNextLine()) {
            String line = in.nextLine();
            String[] slices = line.split(",");
            if("Iris-virginica".equals(slices[4])) {
                line = String.format("+1 1:%s 2:%s 3:%s 4:%s\n", slices[0], slices[1], slices[2], slices[3]);
            } else {
                line = String.format("-1 1:%s 2:%s 3:%s 4:%s\n", slices[0], slices[1], slices[2], slices[3]);
            }
            if(i % 7 < 4) {
                trainWriter.write(line);
            } else {
                testWriter.write(line);
            }
            i += 1;
        }
        trainWriter.flush(); trainWriter.close();
        testWriter.flush(); testWriter.close();
        in.close();
    }
    
    public static void main(String args[]) throws FileNotFoundException, IOException, ClassNotFoundException {
        if(args.length == 1)
            workDir = args[0];
        readyData();
        
        File trainFile = new File(workDir + "/iris_train.data");
        File testFile = new File(workDir + "/iris_test.data");
        File modelFile = new File(workDir + "/iris_train.model");
        File rulesFile = new File(workDir + "/iris_train.rules");
        
        // train
        Dataset trainDataset = new Dataset();
        trainDataset.load(trainFile);
        Dataset testDataset = new Dataset();
        testDataset.load(testFile);
        MartLearnerParams params = new MartLearnerParams();
        params.numCarts = 100;
        params.learningRate = 1;
        params.cartParams.maxDepth = 2;
        params.cartParams.maxNumLeaves = 6;
        params.cartParams.minNumInstances = 4;
        MartNewtonRaphsonLearner learner = new MartNewtonRaphsonLearner();
        learner.setParams(params);
        learner.setModelFile(modelFile);
        MartModel model = learner.learn(trainDataset, testDataset, new BinaryClassificationProblem());
        model.toRuleSetModel().dump(rulesFile);
        
        
        // test
        double precision = .0;
//        Dataset testDataset = new Dataset();
//        testDataset.load(testFile);
        Session session = testDataset.sessions.get(0);
        for(int i = 0; i < session.instances.size(); i++) {
            Instance instance = session.instances.get(i);
            double target = session.targets.get(i);
            double p = model.predict(instance);
            if(p > 0 && target > 0) 
                precision += 1.0;
            else if(p < 0 && target < 0)
                precision += 1.0;
            else
                System.out.println(target + " " + instance);
        }
        precision /= session.instances.size();
        System.out.println(precision);
    }
}
