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
package com.relax.mart.classify;

import com.relax.lib.pcj.*;
import com.relax.lib.*;
import com.relax.mart.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author haimin.shao
 */
public class BinomialClassifierLearner extends Learner<MartModel>{

	private MartLearnerParams params;
	private File modelFile = null;
	private ClassifyDataset trainingSet;
	private ClassifyDataset validatingSet;
	private ClassifyProblem problem;
	
	@Override
	public MartModel resume(MartModel mart) throws InterruptedException, ExecutionException {
		if (mart == null) 
			mart = new MartModel();

		CartLearner weakLearner = new CartLearner();
		weakLearner.setParams(params.cartParams);
		
		// global data initialization
		List<Instance> instances = trainingSet.instances;
		DoubleVector predicts = new DoubleVector(instances.size());
		DoubleVector gradient = new DoubleVector(instances.size());
		DoubleVector secondGradient = new DoubleVector(instances.size());
		
		this.predictAll(predicts, trainingSet, mart);
		
		int m = mart.cartModels.size();
		reportDatasetLoss(m, predicts, mart);
		
		m++;
		for (; m <= params.numCarts; m++) {
			
			// compute gradients
			this.gradientAll(gradient, secondGradient, trainingSet, predicts);
			
			// fit gradients
			weakLearner.setTrainingSet(new RegressDataset(instances, gradient));
			CartModel cart =weakLearner.learn();

//			CartLearnerNode root = weakLearner.learn();
			if (cart.root.left == null) {
				System.out.println("gradients fitting: no obvious gain anymore.");
				break;
			}
			
			// search for best step size
			this.lineSearchAll(cart, predicts, gradient, secondGradient, mart);

			// use simplified version to avoid memory leak
			mart.cartModels.add(cart.simplified());

			// report progress
			reportDatasetLoss(m, predicts, mart);
//			System.out.printf("report loss: %d\n", endTime - startTime);

			dumpModel(mart, modelFile);
		}
		return mart;
	}

	private void reportDatasetLoss(int m, DoubleVector trainPredicts, MartModel mart) throws InterruptedException, ExecutionException {
		Losses trainLosses = new Losses();
		Losses validateLosses = new Losses();
		
		this.lossAll(trainLosses, trainingSet, trainPredicts);
		if(validatingSet != null) {
			DoubleVector validatePredicts = new DoubleVector(validatingSet.instances.size());
			this.predictAll(validatePredicts, validatingSet, mart);
			this.lossAll(validateLosses, validatingSet, validatePredicts);
		}
		
		String currTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		System.out.printf("time %s, iter %03dth , " + 
				"tloss %g, readble tloss %g, vloss %g, readble vloss %g\n",
				currTime, m, trainLosses.loss, trainLosses.readableLoss, validateLosses.loss, validateLosses.readableLoss);
		
	} 
	
	private void lineSearchAll(CartModel cart, DoubleVector predicts, 
			DoubleVector gradient, DoubleVector secondGradient, MartModel mart) throws InterruptedException, ExecutionException {
		List<Future<?>> results = new ArrayList();
		for (CartModelNode modelNode : cart.leaves) {
			CartLearnerNode learnerNode = (CartLearnerNode) modelNode;
			LineSearchTask task = new LineSearchTask();
			task.gradient = gradient;
			task.secondGradient = secondGradient;
			task.mart = mart;
			task.learnerNode = learnerNode;
			task.predicts = predicts;
			results.add(ForkJoinThreadPool.pool.submit(task));
		}
		for(Future<?> future : results)
			future.get();
	}
	
	public class LineSearchTask implements Runnable {
		DoubleVector gradient;
		DoubleVector secondGradient;
		MartModel mart;
		
		//output
		CartLearnerNode learnerNode;
		DoubleVector predicts;
		
		@Override
		public void run() {
			// locate instances
			IntVector selected = new IntVector();
			for (Instance instance : learnerNode.instances) 
				selected.append(instance.offset);
				
			learnerNode.predict = lineSearch(selected, gradient, secondGradient) * params.learningRate;
			
			// update predicts
			for (Instance instance : learnerNode.instances) {;
				double np = predicts.get(instance.offset) + learnerNode.predict;
				predicts.set(np, instance.offset);
			}
		}
		
	}
	
	
	private void lossAll(Losses losses, ClassifyDataset dataset, DoubleVector predicts) throws InterruptedException, ExecutionException {
		List<Future<?>> results = new ArrayList();
		List<LossBatchTask> tasks = new ArrayList();
		int batchSize = 100000;
		
		for(int start = 0; start < dataset.instances.size(); start += batchSize) {
			int end = Math.min(start + batchSize, dataset.instances.size());
			LossBatchTask task = new LossBatchTask();
			task.dataset = dataset;
			task.predicts = predicts;
			task.start = start;
			task.end = end;
			task.losses = new Losses();
			tasks.add(task);
			results.add(ForkJoinThreadPool.pool.submit(task));
		}

		losses.loss = losses.readableLoss = .0;
		for(int i = 0; i < results.size(); i++) {
			results.get(i).get();
			losses.loss += tasks.get(i).losses.loss;
			losses.readableLoss += tasks.get(i).losses.readableLoss;
		}
		losses.readableLoss /= dataset.instances.size();
	}

	private static class Losses {
		double loss = .0;
		double readableLoss = .0;
	}
	
	private class LossBatchTask implements Runnable {
		// input
		ClassifyDataset dataset;
		DoubleVector predicts;
		int start;
		int end;
		// output
		Losses losses;
		
		@Override
		public void run() {
			losses.loss = problem.computeSessionLoss(predicts, dataset, start, end);
			losses.readableLoss = problem.computeReadableSessionLoss(predicts, dataset, start, end);
		}
	}
	
	private void gradientAll(DoubleVector gradient, DoubleVector secondGradient,
			ClassifyDataset dataset, DoubleVector predicts) throws InterruptedException, ExecutionException {
		List<Future<?>> results = new ArrayList();
		int batchSize = 100000;
		for(int start = 0; start < dataset.instances.size(); start += batchSize) {
			int end = Math.min(start + batchSize, dataset.instances.size());
			GradientBatchTask task = new GradientBatchTask();
			task.dataset = dataset;
			task.predicts = predicts;
			task.start = start;
			task.end = end;
			task.gradient = gradient;
			task.secondGradient = secondGradient;
			results.add(ForkJoinThreadPool.pool.submit(task));
		}
		for(Future<?> future : results) {
			future.get();
		}
		
	}

	private class GradientBatchTask implements Runnable {
		ClassifyDataset dataset;
		DoubleVector predicts;
		int start;
		int end;
		
		// output
		DoubleVector gradient;
		DoubleVector secondGradient;
		
		@Override
		public void run() {
			problem.computeSessionLossGradients(predicts, gradient, secondGradient, dataset, start, end);
		}
	}
	
	
	
	private void predictAll(DoubleVector predicts, 
			ClassifyDataset dataset, MartModel mart) throws InterruptedException, ExecutionException {
		List<Future<?>> results = new ArrayList();
		int batchSize = 100000 / (mart.cartModels.size() + 1);
		for(int start = 0; start < dataset.instances.size(); start += batchSize) {
			int end = Math.min(start + batchSize, dataset.instances.size());
			PredictBatchTask task = new PredictBatchTask();
			task.dataset = dataset;
			task.mart = mart;
			task.start = start;
			task.end = end;
			task.predicts = predicts;
			results.add(ForkJoinThreadPool.pool.submit(task));
		}
		for(Future<?> future : results) {
			future.get();
		}
	}
	
	private class PredictBatchTask implements Runnable {
		ClassifyDataset dataset;
		MartModel mart;
		
		int start;
		int end;
		DoubleVector predicts;
		
		@Override
		public void run() {
			for(int i = start; i < end; i++) {
				double p = mart.predict(dataset.instances.get(i));
				predicts.set(p, i);
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
	
	private static double lineSearch(IntVector selected,
			DoubleVector gradient, DoubleVector secondGradient) {
		double sumFirstDerivatives = .0, sumSecondDerivatives = .0;
		int numInstances = 0;

		for (int j = 0; j < selected.size(); j++) {
			int k = selected.get(j);
			sumFirstDerivatives += gradient.get(k);
			sumSecondDerivatives += secondGradient.get(k);
			numInstances += 1;
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
	public void setTrainingSet(ClassifyDataset trainingSet) {
		this.trainingSet = trainingSet;
	}

	/**
	 * @param validatingSet the validatingSet to set
	 */
	public void setValidatingSet(ClassifyDataset validatingSet) {
		this.validatingSet = validatingSet;
	}

	/**
	 * @param problem the problem to set
	 */
	public void setProblem(ClassifyProblem problem) {
		this.problem = problem;
	}
	
	
}
