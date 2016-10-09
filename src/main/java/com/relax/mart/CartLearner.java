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
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author haimin.shao
 */
public class CartLearner extends Learner<CartModel>{

	private CartLearnerParams params;
	private RegressDataset trainingSet;

	public RegressDataset getTrainingSet() {
		return trainingSet;
	}

	public void setTrainingSet(RegressDataset trainingSet) {
		this.trainingSet = trainingSet;
	}

	@Override
	public CartModel learn() throws InterruptedException, ExecutionException {
		List<Instance> instances = trainingSet.instances;
		DoubleVector targets = trainingSet.targets;
		CartLearnerNode root = new CartLearnerNode(instances, targets, 1, params);
		PriorityQueue<CartLearnerNode> leavesToSplit = new PriorityQueue(16, new Comparator<CartLearnerNode>() {
			@Override
			public int compare(CartLearnerNode o1, CartLearnerNode o2) {
				return Double.compare(o2.splitGain, o1.splitGain);
			}
		});
		leavesToSplit.add(root);
		int numLeaves = 1;
		while (!leavesToSplit.isEmpty()) {
			CartLearnerNode node = leavesToSplit.poll();
			if (Math.log(node.seq) / Math.log(2) >= params.maxDepth
					|| node.splitLeftCount < params.minNumExamplesAtLeaf
					|| node.splitRightCount < params.minNumExamplesAtLeaf) {
//				System.out.println("E: " + node.seq + " " + params.maxDepth);
//				System.out.println("F: " + node.splitLeftCount + " " + params.minNumLeafExamples);
//				System.out.println("G: " + node.splitRightCount + " " + params.minNumLeafExamples);
//				System.out.println(node);
				continue;
			}
			if (node.error > params.minRatioSplitGain && node.splitGain > root.error * params.minRatioSplitGain) {
				node.split(params);
				leavesToSplit.add((CartLearnerNode) node.left);
				leavesToSplit.add((CartLearnerNode) node.right);
				numLeaves += 1;
//				System.out.println("H: " + node.seq + " " + params.maxDepth);
				if (numLeaves >= params.maxNumLeaves) {
//					System.out.println("I: " + node.seq + " " + params.maxDepth);
					leavesToSplit.clear();
				}
			} else {
//				System.out.println("J:" + node.error + " " + node.splitGain + " " + params.minRatioSplitGain);
			}
		}
		return new CartModel(root);
	}

	/**
	 * @return the params
	 */
	public CartLearnerParams getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(CartLearnerParams params) {
		this.params = params;
	}

	@Override
	public CartModel resume(CartModel model) throws Exception {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
