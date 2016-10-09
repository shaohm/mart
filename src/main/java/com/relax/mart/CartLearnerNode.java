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

import com.relax.lib.Pair;
import com.relax.lib.ForkJoinThreadPool;
import com.relax.lib.pcj.DoubleVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author haimin.shao
 */
public class CartLearnerNode extends CartModelNode {
	
	
	public List<Instance> instances;
	public DoubleVector targets;
	public int splitLeftCount;
	public int splitRightCount;

	/**
	 * used in learning process. compute all statistics here.
	 *
	 * @param instances
	 * @param targets
	 * @param seq
	 */
	public CartLearnerNode(List<Instance> instances, DoubleVector targets, int seq, CartLearnerParams params) throws InterruptedException, ExecutionException {
		this.instances = instances;
		this.targets = targets;
		this.seq = seq;

		// 采样
		List<Instance> sampledInstances;
		DoubleVector sampledTargets;
		if (this.instances.size() * 0.5 < params.suitableNumExamplesForSplit) {
			sampledInstances = this.instances;
			sampledTargets = this.targets;
		} else {
			TreeSet<Integer> sampledIndices = new TreeSet<Integer>();
			while (sampledIndices.size() < params.suitableNumExamplesForSplit) {
				int index = (int) (Math.random() * instances.size());
				sampledIndices.add(index);
			}
			sampledInstances = new ArrayList<>(params.suitableNumExamplesForSplit);
			sampledTargets = new DoubleVector(params.suitableNumExamplesForSplit);
			for (int index : sampledIndices) {
				sampledInstances.add(this.instances.get(index));
				sampledTargets.append(this.targets.get(index));
			}
		}

		// 收集所有的特征
		Set<Integer> featureSet = new TreeSet<Integer>();
		double tSum = .0;
		double tSquaredSum = .0;
		int countAll = 0;
		List<Pair<Instance, Double>> examples = new ArrayList();

		for (int i = 0; i < sampledInstances.size(); i++) {
			Instance instance = sampledInstances.get(i);
			double target = sampledTargets.get(i);
			tSum += target;
			tSquaredSum += target * target;
			countAll += 1;
			examples.add(new Pair(instance, target));
			for (int j = 0; j < instance.getSize(); j++) {
				int featureIndex = instance.indexAt(j);
				featureSet.add(featureIndex);
			}
		}

		// find the best split
		List<SplitSearcher> tasks = new ArrayList();
		for(int feature : featureSet) {
			SplitSearcher searcher = new SplitSearcher();
			searcher.examples = new ArrayList(examples);
			searcher.feature = feature;
			searcher.params = params;
			searcher.tSum = tSum;
			searcher.tSquaredSum = tSquaredSum;
			tasks.add(searcher);
		}
		List<Future<Split>> results = ForkJoinThreadPool.pool.invokeAll(tasks);
		
		Split bestSplit = null;
		for(Future<Split> future : results) {
			Split split = future.get();
			if(bestSplit == null || bestSplit.gain < split.gain) {
				bestSplit = split;
			}
		}

		this.numInstances = this.instances.size();
		this.error = tSquaredSum - tSum * tSum / countAll;
		this.predict = tSum / countAll;
		this.splitFeature = bestSplit.feature;
		this.splitValue = (bestSplit.startValue + bestSplit.endValue) / 2;
		this.splitGain = bestSplit.gain;
		this.splitLeftCount = bestSplit.leftCount;
		this.splitRightCount = bestSplit.rightCount;
	}
	
	
	private static class ExampleComparator implements Comparator<Pair<Instance, Double>> {
		int feature;
		public ExampleComparator(int feature) {
			this.feature = feature;
		}
		@Override
		public int compare(Pair<Instance, Double> o1, Pair<Instance, Double> o2) {
			double v1 = o1.first.findValue(feature);
			double v2 = o2.first.findValue(feature);
			return Double.compare(v1, v2);
		}
	}

	private static class Split implements Comparable<Split> {
		int feature;
		double startValue = .0, endValue = .0;
		double gain = .0;
		int leftCount = 0, rightCount = 0;
		
		@Override
		public int compareTo(Split o) {
			return Double.compare(this.gain, o.gain);
		}
	}
	
	private static class SplitSearcher implements  Callable<Split> {
		List<Pair<Instance, Double>> examples;
		int feature;
		CartLearnerParams params;
		
		double tSum = .0;
		double tSquaredSum = .0;
		
		@Override
		public Split call() {
			int countAll = examples.size();
			double errorTotal = tSquaredSum - tSum * tSum / countAll;
			Collections.sort(examples, new ExampleComparator(feature));
			double tSumLeft = .0, tSumRight = tSum;
			double tSquaredSumLeft = .0, tSquaredSumRight = tSquaredSum;
			int countLeft = 0, countRight = countAll;
			
			Split bestSplit = new Split();
			bestSplit.feature = this.feature;
			
			double prevValue = .0;
			boolean inBestRegion = false;
			for (int i = 0; i < countAll; i++) {
				Pair<Instance, Double> e = examples.get(i);
				double currValue = e.first.findValue(feature);
				if (i > 0 && prevValue != currValue) {
					double errorLeft = tSquaredSumLeft - (countLeft > 0 ? tSumLeft * tSumLeft / countLeft : 0);
					double errorRight = tSquaredSumRight - (countRight > 0 ? tSumRight * tSumRight / countRight : 0);
					double gain = errorTotal - errorLeft - errorRight;
					if (gain > bestSplit.gain) {
						if (countLeft >= params.minNumExamplesAtLeaf && countRight >= params.minNumExamplesAtLeaf) {
							bestSplit.startValue = bestSplit.endValue = prevValue;
							bestSplit.gain = gain;
							bestSplit.leftCount = countLeft;
							bestSplit.rightCount = countRight;
							inBestRegion = true;
						}
					} else if (gain < bestSplit.gain && inBestRegion) {
						if (countLeft >= params.minNumExamplesAtLeaf && countRight >= params.minNumExamplesAtLeaf) {
							bestSplit.endValue = prevValue;
							inBestRegion = false;
						}
					}
				}
				tSumLeft += e.second;
				tSumRight -= e.second;
				tSquaredSumLeft += e.second * e.second;
				tSquaredSumRight -= e.second * e.second;
				countLeft++;
				countRight--;
				prevValue = currValue;
			}
			return bestSplit;
		}
	}
	
	
	
	public void split(CartLearnerParams params) throws InterruptedException, ExecutionException {
		if (this.splitFeature < 0) {
			throw new RuntimeException("split not found");
		}
		List<Instance> instancesLeft = new ArrayList(), instancesRight = new ArrayList();
		DoubleVector targetsLeft = new DoubleVector(), targetsRight = new DoubleVector();
		for (int i = 0; i < this.instances.size(); i++) {
			Instance instance = this.instances.get(i);
			double target = this.targets.get(i);
			double value = instance.findValue(this.splitFeature);
			if (value <= this.splitValue) {
				instancesLeft.add(instance);
				targetsLeft.append(target);
			} else {
				instancesRight.add(instance);
				targetsRight.append(target);
			}
		}
		this.left = new CartLearnerNode(instancesLeft, targetsLeft, this.seq * 2, params);
		this.left.parent = this;
		this.right = new CartLearnerNode(instancesRight, targetsRight, this.seq * 2 + 1, params);
		this.right.parent = this;
	}
}
