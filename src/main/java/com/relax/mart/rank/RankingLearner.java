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
package com.relax.mart.rank;

import com.relax.lib.pcj.DoubleVector;
import com.relax.lib.pcj.IntVector;
import com.relax.mart.CartLearner;
import com.relax.mart.CartLearnerNode;
import com.relax.mart.CartModel;
import com.relax.mart.CartModelNode;
import com.relax.lib.ForkJoinThreadPool;
import com.relax.mart.Instance;
import com.relax.mart.Learner;
import com.relax.mart.MartLearnerParams;
import com.relax.mart.MartModel;
import com.relax.mart.RegressDataset;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author haimin.shao
 */
public class RankingLearner extends Learner<MartModel>{

	private MartLearnerParams params;
	private File modelFile = null;
	private RankDataset trainingSet;
	private RankDataset validatingSet;
	private RankProblem problem;
	
	public MartModel resume(MartModel mart) throws InterruptedException, ExecutionException {
		if (mart == null) 
			mart = new MartModel();

		CartLearner weakLearner = new CartLearner();
		weakLearner.setParams(params.cartParams);
		
		// global data initialization
		List<Session> sessions = trainingSet.sessions;
		List<DoubleVector> predictsList = new ArrayList(sessions.size());
		List<DoubleVector> gradientList = new ArrayList(sessions.size());
		List<DoubleVector> secondGradientList = new ArrayList(sessions.size());		
		List<Instance> instances = new ArrayList<Instance>();
		
		
		for (Session session : sessions) {
			instances.addAll(session.instances);
			predictsList.add(new DoubleVector(session.targets.size()));
			gradientList.add(new DoubleVector(session.targets.size()));
			secondGradientList.add(new DoubleVector(session.targets.size()));
		}
		this.predictAll(predictsList, sessions, mart);
		
		
		int m = mart.cartModels.size();
		reportDatasetLoss(m, predictsList, mart);
		
		m++;
		DoubleVector gradients = new DoubleVector(instances.size());
		for (; m <= params.numCarts; m++) {
			long startTime = 0, endTime = 0;
			
			// compute gradients
			startTime = System.currentTimeMillis();
			gradients.clear(0);
			this.gradientAll(gradientList, secondGradientList, sessions, predictsList);
			for (int i = 0; i < sessions.size(); i++) {
				gradients.append(gradientList.get(i));
			}
			endTime = System.currentTimeMillis();
//			System.out.printf("compute gradients: %d\n", endTime - startTime);
			
			// fit gradients
			startTime = System.currentTimeMillis();
			weakLearner.setTrainingSet(new RegressDataset(instances, gradients));
			CartModel cart = weakLearner.learn();
			
			if (cart.root.left == null) {
				System.out.println("gradients fitting: no obvious gain anymore.");
				break;
			}
			endTime = System.currentTimeMillis();
//			System.out.printf("fit gradients: %d\n", endTime - startTime);
			
			// search for best step size
			startTime = System.currentTimeMillis();
			this.lineSearchAll(cart, predictsList, gradientList, secondGradientList, mart);
			endTime = System.currentTimeMillis();
//			System.out.printf("line search: %d\n", endTime - startTime);

			// use simplified version to avoid memory leak
			mart.cartModels.add(cart.simplified());

			// report progress
			startTime = System.currentTimeMillis();
			reportDatasetLoss(m, predictsList, mart);
			endTime = System.currentTimeMillis();
//			System.out.printf("report loss: %d\n", endTime - startTime);

			dumpModel(mart, modelFile);
		}
		return mart;
	}

	private void reportDatasetLoss(int m, List<DoubleVector> trainPredictsList, MartModel mart) throws InterruptedException, ExecutionException {
		double trainLoss = .0;
		double trainReadableLoss = .0;
		double validateLoss = .0;
		double validateReadableLoss = .0;
		
		DoubleVector trainLosses = new DoubleVector(trainingSet.sessions.size());
		DoubleVector trainReadableLosses = new DoubleVector(trainingSet.sessions.size());

		this.lossAll(trainLosses, trainReadableLosses, trainingSet.sessions, trainPredictsList);
		
		for(int i = 0; i < trainLosses.size(); i++)
			trainLoss += trainLosses.get(i);
		
		for(int i = 0; i < trainReadableLosses.size(); i++)
			trainReadableLoss += trainReadableLosses.get(i);
		trainReadableLoss /= trainReadableLosses.size();
		
		if(validatingSet != null) {
			DoubleVector validateLosses = new DoubleVector(validatingSet.sessions.size());
			DoubleVector validateReadableLosses = new DoubleVector(validatingSet.sessions.size());
			List<DoubleVector> validatePredictsList = new ArrayList();

			for (Session session : validatingSet.sessions) {
				DoubleVector predicts = new DoubleVector(session.instances.size());
				validatePredictsList.add(predicts);
			}
			this.predictAll(validatePredictsList, validatingSet.sessions, mart);
			this.lossAll(validateLosses, validateReadableLosses, validatingSet.sessions, validatePredictsList);

			for(int i = 0; i < validateLosses.size(); i++)
				validateLoss += validateLosses.get(i);

			for(int i = 0; i < validateReadableLosses.size(); i++)
				validateReadableLoss += validateReadableLosses.get(i);
			validateReadableLoss /= validateReadableLosses.size();
		}
		String currTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		System.out.printf("time %s, iter %03dth , " + 
				"tloss %g, readble tloss %g, vloss %g, readble vloss %g\n",
				currTime, m, trainLoss, trainReadableLoss, validateLoss, validateReadableLoss);
		
	} 
	
	private void lineSearchAll(CartModel cart, List<DoubleVector> predictsList, 
			List<DoubleVector> gradientList, List<DoubleVector> secondGradientList, MartModel mart) throws InterruptedException, ExecutionException {
		List<Future<?>> results = new ArrayList();
		for (CartModelNode modelNode : cart.leaves) {
			CartLearnerNode learnerNode = (CartLearnerNode) modelNode;
			LineSearchTask task = new LineSearchTask();
			task.gradientList = gradientList;
			task.secondGradientList = secondGradientList;
			task.mart = mart;
			task.learnerNode = learnerNode;
			task.predictsList = predictsList;
			results.add(ForkJoinThreadPool.pool.submit(task));
		}
		for(Future<?> future : results)
			future.get();
	}
	
	public class LineSearchTask implements Runnable {
		List<DoubleVector> gradientList;
		List<DoubleVector> secondGradientList;
		MartModel mart;
		
		//output
		CartLearnerNode learnerNode;
		List<DoubleVector> predictsList;
		
		@Override
		public void run() {
			// locate instances
			Map<Integer, IntVector> sessionMap = new TreeMap();
			Iterator<Instance> iter = learnerNode.instances.iterator();
			while(iter.hasNext()) {
				RankInstance instance = (RankInstance)iter.next();
				if (sessionMap.containsKey(instance.session.offset)) {
					sessionMap.get(instance.session.offset).append(instance.offset);
				} else {
					IntVector selected = new IntVector(4);
					selected.append(instance.offset);
					sessionMap.put(instance.session.offset, selected);
				}
			}

			learnerNode.predict = lineSearch(sessionMap, gradientList, secondGradientList);
			// update predicts
			iter = learnerNode.instances.iterator();
			while(iter.hasNext()) {
				RankInstance instance = (RankInstance)iter.next();
				DoubleVector predicts = predictsList.get(instance.session.offset);
				double np = predicts.get(instance.offset) + params.learningRate * learnerNode.predict;
				predicts.set(np, instance.offset);
			}
		}
		
	}
	
	private void lossAll(DoubleVector losses, DoubleVector readableLosses, 
			List<Session> sessions, List<DoubleVector> predictsList) throws InterruptedException, ExecutionException {
		List<DoubleVector> lossesList = new ArrayList<DoubleVector>();
		List<DoubleVector> readableLossesList = new ArrayList<DoubleVector>();
		
		List<Future<?>> results = new ArrayList();
		int batchSize = 100000;
		for(int start = 0; start < sessions.size(); start += batchSize) {
			int end = Math.min(start + batchSize, sessions.size());
			LossBatchTask task = new LossBatchTask();
			task.sessions = sessions.subList(start, end);
			task.predictsList = predictsList.subList(start, end);
			task.losses = new DoubleVector(end - start);
			task.readableLosses = new DoubleVector(end - start);
			
			lossesList.add(task.losses);
			readableLossesList.add(task.readableLosses);
			results.add(ForkJoinThreadPool.pool.submit(task));
		}
		losses.clear(0); 
		readableLosses.clear(0);
		for(int i = 0; i < results.size(); i++) {
			results.get(i).get();
			losses.append(lossesList.get(i));
			readableLosses.append(readableLossesList.get(i));
		}		
	}
	
	
	
	private class LossBatchTask implements Runnable {
		// input
		List<Session> sessions;
		List<DoubleVector> predictsList;
		
		// output
		DoubleVector losses;
		DoubleVector readableLosses;
		
		@Override
		public void run() {
			for(int i = 0; i < sessions.size(); i++) {
				DoubleVector localPredicts = predictsList.get(i);
				losses.set(problem.computeSessionLoss(localPredicts, sessions.get(i)), i);
				readableLosses.set(problem.computeReadableSessionLoss(localPredicts, sessions.get(i)), i);
			}
		}
	}
	
	private void gradientAll(List<DoubleVector> gradientList, List<DoubleVector> secondGradientList,
			List<Session> sessions, List<DoubleVector> predictsList) throws InterruptedException, ExecutionException {
		List<Future<?>> results = new ArrayList();
		int batchSize = 100000;
		for(int start = 0; start < sessions.size(); start += batchSize) {
			int end = Math.min(start + batchSize, sessions.size());
			GradientBatchTask task = new GradientBatchTask();
			task.sessions = sessions.subList(start, end);
			task.predictsList = predictsList.subList(start, end);
			task.gradientList = gradientList.subList(start, end);
			task.secondGradientList = secondGradientList.subList(start, end);
			results.add(ForkJoinThreadPool.pool.submit(task));
		}
		for(Future<?> future : results) {
			future.get();
		}
		
	}

	private class GradientBatchTask implements Runnable {
		List<Session> sessions;
		List<DoubleVector> predictsList;
		List<DoubleVector> gradientList;
		List<DoubleVector> secondGradientList;
		
		@Override
		public void run() {
			for(int i = 0; i < sessions.size(); i++) {
				DoubleVector localPredicts = predictsList.get(i);
				DoubleVector localGradient = gradientList.get(i);
				DoubleVector localSecondGradient = secondGradientList.get(i);
				problem.computeSessionLossGradients(localPredicts, localGradient, localSecondGradient, sessions.get(i));
			}
		}
	}
	
	
	
	private void predictAll(List<DoubleVector> predictsList, 
			List<Session> sessions, MartModel mart) throws InterruptedException, ExecutionException {
		List<Future<?>> results = new ArrayList();
		int batchSize = 100000 / (mart.cartModels.size() + 1);
		for(int start = 0; start < sessions.size(); start += batchSize) {
			int end = Math.min(start + batchSize, sessions.size());
			PredictBatchTask task = new PredictBatchTask();
			task.sessions = sessions.subList(start, end);
			task.predictsList = predictsList.subList(start, end);
			task.mart = mart;
			results.add(ForkJoinThreadPool.pool.submit(task));
		}
		for(Future<?> future : results) {
			future.get();
		}
	}
	
	private class PredictBatchTask implements Runnable {
		List<Session> sessions;
		List<DoubleVector> predictsList;
		MartModel mart;
		
		@Override
		public void run() {
			for(int i = 0; i < sessions.size(); i++) {
				List<RankInstance> instances = sessions.get(i).instances;
				DoubleVector predicts = predictsList.get(i);
				for(int j = 0; j < instances.size(); j++) {
					double p = mart.predict(instances.get(j));
					predicts.set(p, j);
				}
			}
		}
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

	/**
	 * @param trainingSet the trainingSet to set
	 */
	public void setTrainingSet(RankDataset trainingSet) {
		this.trainingSet = trainingSet;
	}

	/**
	 * @param validatingSet the validatingSet to set
	 */
	public void setValidatingSet(RankDataset validatingSet) {
		this.validatingSet = validatingSet;
	}

	/**
	 * @param problem the problem to set
	 */
	public void setProblem(RankProblem problem) {
		this.problem = problem;
	}
	
	
}
