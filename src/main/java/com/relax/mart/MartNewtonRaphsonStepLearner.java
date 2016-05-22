/*
 * Copyright 2016 haimin.shao.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class MartNewtonRaphsonStepLearner {

	private MartLearnerParams params;
	private File modelFile = null;

	public MartModel learn(Dataset trainingSet, Dataset validatingSet, Problem problem) {
		return this.resume(null, trainingSet, validatingSet, problem);
	}
	
	public MartModel resume(MartModel mart, Dataset trainingSet, Dataset validatingSet, Problem problem) {
		if (mart == null) {
			mart = new MartModel(params.learningRate);
		} else if(mart.learningRate == 0 && mart.cartModels.isEmpty()){
			mart.learningRate = params.learningRate;
		} else {
			if (mart.learningRate != params.learningRate) {
				throw new IllegalArgumentException("Two different learning rate settings.");
			}
		}

		List<Session> sessions = trainingSet.sessions;
		CartLearner weakLearner = new CartLearner();
		weakLearner.setParams(params.cartParams);
		List<DoubleVector> predictsList = new ArrayList(sessions.size());
		List<DoubleVector> gradientList = new ArrayList(sessions.size());
		List<DoubleVector> secondGradientList = new ArrayList(sessions.size());
		List<Instance> instances = new ArrayList<Instance>();
		for (Session session : sessions) {
			instances.addAll(session.instances);
			DoubleVector localPredicts = new DoubleVector(session.targets.size());
			for (Instance instance : session.instances) {
				localPredicts.append(mart.predict(instance));
			}
			predictsList.add(localPredicts);
			gradientList.add(new DoubleVector(session.targets.size()));
			secondGradientList.add(new DoubleVector(session.targets.size()));
		}

		int m = mart.cartModels.size();
		reportDatasetLoss(trainingSet, validatingSet, mart, problem, m);
		
		m++;
		DoubleVector gradients = new DoubleVector(instances.size());
		for (; m <= params.numCarts; m++) {
			// compute gradients
			gradients.clear(0);
			for (int i = 0; i < sessions.size(); i++) {
				DoubleVector localPredicts = predictsList.get(i);
				DoubleVector localGradient = gradientList.get(i);
				DoubleVector localSecondGradient = secondGradientList.get(i);
				problem.computeSessionLossGradients(localPredicts, localGradient, localSecondGradient, sessions.get(i));
				gradients.append(localGradient);
			}

			// fit gradients
			CartLearnerNode root = weakLearner.learn(instances, gradients, problem);
			if (root.left == null) {
				System.out.println("gradients fitting: no obvious gain anymore.");
				break;
			}
			
			// search for best step size
			CartModel cart = new CartModel(root);
			for (CartModelNode modelNode : cart.leaves) {
				CartLearnerNode learnerNode = (CartLearnerNode) modelNode;
				Map<Integer, IntVector> sessionMap = new TreeMap();
				
				// debug
				if(learnerNode.instances.isEmpty()) {
					System.out.println("l:" + learnerNode.toString());
					System.out.println("l:" + learnerNode.parent.toString());
					System.out.println("l:" + learnerNode.parent.left.toString());
					System.out.println("l:" + learnerNode.parent.right.toString());
				}
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

				for (Instance instance : learnerNode.instances) {
					DoubleVector predicts = predictsList.get(instance.sesstion.offset);
					double np = predicts.get(instance.offset) + mart.learningRate * learnerNode.predict;
					predicts.set(np, instance.offset);
				}
			}

			// use simplified version to avoid memory leak
			mart.cartModels.add(cart.simplified());

			// report progress
			reportDatasetLoss(trainingSet, validatingSet, mart, problem, m);
			dumpModel(mart, modelFile);
		}
		return mart;
	}

	private static void dumpModel(MartModel mart, File modelFile) {
		if (modelFile != null) {
			try {
				mart.dump(modelFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}




	private static void reportDatasetLoss(Dataset trainDs, Dataset validateDs, MartModel mart, Problem problem, int m) {
		// 计算和报告新的损失
		double trainLoss = .0;
		double trainReadableLoss = .0;
		double validateReadableLoss = .0;
		DoubleVector predicts = new DoubleVector();
		for(Session session : trainDs.sessions) {
			predicts.clear(0);
			for(Instance instance : session.instances)
				predicts.append(mart.predict(instance));
			trainLoss += problem.computeSessionLoss(predicts, session);
			trainReadableLoss += problem.computeReadableSessionLoss(predicts, session);
		}
		trainReadableLoss /= trainDs.sessions.size();

		if (validateDs != null) {
			for (Session session : validateDs.sessions) {
				predicts.clear(0);
				for(Instance instance : session.instances)
					predicts.append(mart.predict(instance));
				validateReadableLoss += problem.computeReadableSessionLoss(predicts, session);
			}
			validateReadableLoss /= validateDs.sessions.size();
		}
		System.out.printf("after the %03dth iteration, total loss is %g, readble loss is %g, validate loss is %g, \n",
				m, trainLoss, trainReadableLoss, validateReadableLoss);

	}

	private static double lineSearch(Map<Integer, IntVector> selected,
			List<DoubleVector> gradientList, List<DoubleVector> secondGradientList) {
		double sumFirstDerivatives = .0, sumSecondDerivatives = .0;
		int numInstances = 0;
		for (Map.Entry<Integer, IntVector> entry : selected.entrySet()) {
			int i = entry.getKey();
			IntVector indices = entry.getValue();
			for (int j = 0; j < indices.size(); j++) {
				int k = indices.get(j);
				sumFirstDerivatives += gradientList.get(i).get(k);
				sumSecondDerivatives += secondGradientList.get(i).get(k);
				numInstances += 1;
			}
		}
		double step = -sumFirstDerivatives / sumSecondDerivatives;
		if(Double.isNaN(step) || Math.abs(step) > 100) {
			System.out.println("dd:" + selected.size() + " " + numInstances + " " + sumFirstDerivatives + " " + sumSecondDerivatives);
		}
		step = step < -10 ? -10 : step;
		step = step > +10 ? +10 : step;
		return step;
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
