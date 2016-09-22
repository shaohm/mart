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

	public static void readyData() throws Exception {
		File originFile = new File(workDir + "/iris.data");
		File trainFile = new File(workDir + "/iris_train.data");
		File testFile = new File(workDir + "/iris_test.data");
		Scanner in = new Scanner(originFile, "utf-8");
		PrintWriter trainWriter = new PrintWriter(trainFile, "utf-8");
		PrintWriter testWriter = new PrintWriter(testFile, "utf-8");
		int i = 0;
		while (in.hasNextLine()) {
			String line = in.nextLine();
			String[] slices = line.split(",");
			if ("Iris-virginica".equals(slices[4])) {
				line = String.format("+1 1:%s 2:%s 3:%s 4:%s\n", slices[0], slices[1], slices[2], slices[3]);
			} else {
				line = String.format("-1 1:%s 2:%s 3:%s 4:%s\n", slices[0], slices[1], slices[2], slices[3]);
			}
			if (i % 7 < 4) {
				trainWriter.write(line);
			} else {
				testWriter.write(line);
			}
			i += 1;
		}
		trainWriter.flush();
		trainWriter.close();
		testWriter.flush();
		testWriter.close();
		in.close();
	}

	public static void main(String args[]) throws Exception {
		if (args.length == 1) {
			workDir = args[0];
		}
		readyData();

		File trainFile = new File(workDir + "/iris_train.data");
		File testFile = new File(workDir + "/iris_test.data");
		File modelFile = new File(workDir + "/iris_train.model");
		File rulesFile = new File(workDir + "/iris_train.rules");

		// train
		Dataset trainDataset = new Dataset();
		trainDataset.load(trainFile);
		Dataset validateDataSet = new Dataset();
		validateDataSet.load(testFile);
		MartLearnerParams params = new MartLearnerParams();
		params.numCarts = 100;
		params.learningRate = 1;
		params.cartParams.maxDepth = 2;
		params.cartParams.maxNumLeaves = 6;
		params.cartParams.minNumExamplesAtLeaf = 4;
		BinomialClassificationProblem problem = new BinomialClassificationProblem();

		MartNewtonRaphsonStepLearner learner = new MartNewtonRaphsonStepLearner();
		learner.setParams(params);
		learner.setModelFile(modelFile);
		learner.setProblem(problem);
		learner.setTrainingSet(trainDataset);
		learner.setValidatingSet(validateDataSet);
		MartModel model = learner.learn();
		model.toRuleSetModel().dump(rulesFile);

		// test
		double precision = .0;
//        Dataset testDataset = new Dataset();
//        testDataset.load(testFile);
		Session session = validateDataSet.sessions.get(0);
		for (int i = 0; i < session.instances.size(); i++) {
			Instance instance = session.instances.get(i);
			double target = session.targets.get(i);
			double p = model.predict(instance);
			if (p > 0 && target > 0) {
				precision += 1.0;
			} else if (p < 0 && target < 0) {
				precision += 1.0;
			} else {
				System.out.println(target + " " + instance);
			}
		}
		precision /= session.instances.size();
		System.out.println(precision);
	}
}
