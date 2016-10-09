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
package com.relax.mart.expriment;

import com.relax.mart.classify.*;
import com.relax.mart.*;
import java.io.*;
/**
 *
 * @author haimin.shao
 */
public class MnistExperiment {

	public static void main(String args[]) throws Exception {
		File trainFile = new File(args[0]);
		File testFile = new File(args[1]);
		File modelFile = new File(args[2]);
		File rulesFile = new File(args[3]);

		// train
		ClassifyDataset trainDataset = new ClassifyDataset();
		trainDataset.load(trainFile);
		ClassifyDataset validateDataset = new ClassifyDataset();
		validateDataset.load(testFile);
		MartLearnerParams params = new MartLearnerParams();
		params.numCarts = 100;
		params.learningRate = 1;
		params.cartParams.maxDepth = 3;
		params.cartParams.maxNumLeaves = 5;
		params.cartParams.minNumExamplesAtLeaf = 6;
		params.cartParams.suitableNumExamplesForSplit = 3000;
		
		BinomialClassifyProblem problem = new BinomialClassifyProblem();
		BinomialClassifierLearner learner = new BinomialClassifierLearner();
		learner.setParams(params);
		learner.setModelFile(modelFile);
		learner.setProblem(problem);
		learner.setTrainingSet(trainDataset);
		learner.setValidatingSet(validateDataset);
//		MartModel model = learner.learn(trainDataset, testDataset, new BipartitePairwiseRankProblem());
		MartModel model = learner.learn();
		model.toRuleSetModel().dump(rulesFile);
	}
}
