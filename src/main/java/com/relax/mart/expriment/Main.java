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

import com.relax.mart.rank.RankDataset;
import com.relax.mart.rank.BipartitePairwiseRankProblem;
import com.relax.mart.MartLearnerParams;
import com.relax.mart.MartModel;
import com.relax.mart.rank.RankingLearner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;

/**
 *
 * @author haimin.shao
 */
public class Main {

	static String workDir = "D:\\SoftwareData\\rhcygwin64\\home\\haimin.shao\\code\\rankboost_code";

	public static void main(String args[]) throws Exception {
		File fromFile = new File(workDir + "\\w.dat");
		File toFile = new File(workDir + "\\v.dat");
		File to2File = new File(workDir + "\\u.dat");
		RankDataset trainDataset = new RankDataset();
		trainDataset.load(fromFile);

		MartLearnerParams params = new MartLearnerParams();
		params.numCarts = 100;
		params.learningRate = 1;
//        params.cartParams = new CartLearnerParams();
		params.cartParams.maxDepth = 1;
		params.cartParams.maxNumLeaves = 6;
		params.cartParams.minNumExamplesAtLeaf = 4;
		BipartitePairwiseRankProblem problem = new BipartitePairwiseRankProblem();
		RankingLearner learner = new RankingLearner();
		learner.setParams(params);
		learner.setProblem(problem);
		learner.setTrainingSet(trainDataset);
		learner.setValidatingSet(null);
		problem.readableLossTopN = 1;
		MartModel model = learner.learn();
		model.dump(toFile);
//        System.out.println(model.toString());
		MartModel model2 = new MartModel();
		model2.load(toFile);
		model2.dump(to2File);
//		Collections.sort(null);
	}
}
