/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

import com.relax.lib.pcj.DoubleVector;
import com.relax.lib.pcj.IntVector;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author haimin.shao
 */
public class MartNewtonRaphsonLearner {

    private MartLearnerParams params;
    private File modelFile = null;


    public MartModel learn(Dataset trainingSet, Dataset validatingSet, Problem problem) {
        List<Session> sessions = trainingSet.sessions;

        CartLearner weakLearner = new CartLearner();
        weakLearner.setParams(params.cartParams);

        MartModel mart = new MartModel(params.learningRate);

        // 初始化targets, predicts, losses
        List<DoubleVector> targetsList = new ArrayList(sessions.size());
        List<DoubleVector> predictsList = new ArrayList(sessions.size());
        List<DoubleVector> gradientList = new ArrayList(sessions.size());
        List<DoubleVector> secondGradientList = new ArrayList(sessions.size());
        DoubleVector losses = new DoubleVector(sessions.size());

        List<Instance> instances = new ArrayList<Instance>();
        for (Session session : sessions) {
            instances.addAll(session.instances);
            targetsList.add(session.targets);
            DoubleVector localPredicts = new DoubleVector(0.0, session.targets.size());
            predictsList.add(localPredicts);
            DoubleVector localGradient = new DoubleVector(0.0, session.targets.size());
            gradientList.add(localGradient);
            DoubleVector localSecondGradient = new DoubleVector(0.0, session.targets.size());
            secondGradientList.add(localSecondGradient);

            double loss = problem.computeSessionLoss(session.targets, localPredicts, null);
            losses.append(loss);
        }
        
        // 计算初始的损失
        double totalLoss = .0, readableLoss = .0, validateLoss = .0;
        for (int i = 0; i < sessions.size(); i++) {
            DoubleVector targets = targetsList.get(i);
            DoubleVector predicts = predictsList.get(i);
            totalLoss += problem.computeSessionLoss(targets, predicts, null);
            readableLoss += problem.computeReadableSessionLoss(targets, predicts, null);
        }
        readableLoss /= sessions.size();
        
        if(validatingSet != null) {
            for (Session session : validatingSet.sessions) {
                DoubleVector targets = session.targets;
                DoubleVector predicts = new DoubleVector(targets.size());
                for(Instance instance : session.instances) 
                    predicts.append(mart.predict(instance));
                validateLoss += problem.computeReadableSessionLoss(targets, predicts, null);
            }
            validateLoss /= validatingSet.sessions.size();
        }
        System.out.printf("after the %03dth iteration, total loss is %g, readble loss is %g, validate loss is %g, \n", 
                    0, totalLoss, readableLoss, validateLoss);
        
        DoubleVector gradient = new DoubleVector(instances.size());
        for (int m = 1; m <= params.numCarts; m++) {
            // 计算梯度
            gradient.clear(0);
            for (int i = 0; i < sessions.size(); i++) {
                DoubleVector localTargets = targetsList.get(i);
                DoubleVector localPredicts = predictsList.get(i);
                DoubleVector localGradient = gradientList.get(i);
                DoubleVector localSecondGradient = secondGradientList.get(i);

                problem.computeSessionLossGradients(localTargets, localPredicts, localGradient, localSecondGradient, null);
                gradient.append(localGradient);
            }

            // 训练弱学习机
            CartLearnerNode root = weakLearner.learn(instances, gradient, problem);
            if(root.left == null) {
                System.out.println("learn nothing");
                break;
            }
            CartModel cart = new CartModel(root);

            // 搜索每个叶子节点的最佳步长
            for (CartModelNode modelNode : cart.leaves) {
                CartLearnerNode learnerNode = (CartLearnerNode) modelNode;
                Map<Integer, IntVector> sessionMap = new TreeMap();
                for (Instance instance : learnerNode.instances) {
                    if (sessionMap.containsKey(instance.sesstion.offset)) {
                        sessionMap.get(instance.sesstion.offset).append(instance.offset);
                    } else {
                        IntVector selected = new IntVector(4);
                        selected.append(instance.offset);
                        sessionMap.put(instance.sesstion.offset, selected);
                    }
                }
                learnerNode.predict = lineSearch(sessionMap, gradientList, secondGradientList);
                
                // 处理完每个叶子节点，都更新预测值，让下个叶子节点的linesearch能用到最新的预测值，以保证收敛
                for (Instance instance : learnerNode.instances) {
                    DoubleVector predicts = predictsList.get(instance.sesstion.offset);
                    double np = predicts.get(instance.offset) + mart.learningRate * learnerNode.predict;
                    predicts.set(np, instance.offset);
                }
            }

            // 融合, 注意瘦身，避免内存泄漏
            cart.simplify();
            mart.cartModels.add(cart);

            // 计算和报告新的损失
            totalLoss = .0;
            readableLoss = .0; 
            for (int i = 0; i < sessions.size(); i++) {
                DoubleVector targets = targetsList.get(i);
                DoubleVector predicts = predictsList.get(i);
                losses.set(problem.computeSessionLoss(targets, predicts, null), i);
                totalLoss += losses.get(i);
                readableLoss += problem.computeReadableSessionLoss(targets, predicts, null);
            }
            readableLoss /= sessions.size();
            
            validateLoss = .0;
            if(validatingSet != null) {
                for (Session session : validatingSet.sessions) {
                    DoubleVector targets = session.targets;
                    DoubleVector predicts = new DoubleVector(targets.size());
                    for(Instance instance : session.instances) 
                        predicts.append(mart.predict(instance));
                    validateLoss += problem.computeReadableSessionLoss(targets, predicts, null);
                }
                validateLoss /= validatingSet.sessions.size();
            }
            System.out.printf("after the %03dth iteration, total loss is %g, readble loss is %g, validate loss is %g, \n", 
                    m, totalLoss, readableLoss, validateLoss);
            
            if(modelFile != null) {
                try {
                    mart.dump(modelFile);
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return mart;
    }

    
    
    private static double lineSearch(Map<Integer, IntVector> selected, 
            List<DoubleVector> gradientList, List<DoubleVector> secondGradientList) {
        double sumFirstDerivatives = .0, sumSecondDerivatives = .0; 
        for (Map.Entry<Integer, IntVector> entry : selected.entrySet()) {
            int i = entry.getKey();
            IntVector indices = entry.getValue();
            for(int j = 0; j < indices.size(); j++) {
                int k = indices.get(j);
                sumFirstDerivatives += gradientList.get(i).get(k);
                sumSecondDerivatives += secondGradientList.get(i).get(k);
            }
        }
        return - sumFirstDerivatives / sumSecondDerivatives;
    }

    /**
     * @return the params
     */
    public MartLearnerParams getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(MartLearnerParams params) {
        this.params = params;
    }

    public File getModelFile() {
        return modelFile;
    }

    public void setModelFile(File modelFile) {
        this.modelFile = modelFile;
    }
    
}
