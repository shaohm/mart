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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author haimin.shao
 */
public class MultinomialClassifierLearner extends Learner<MartSeriesModel>{

	private MartLearnerParams params;
	private File modelFile = null;
	private ClassifyDataset trainingSet;
	private ClassifyDataset validatingSet;
	private ClassifyProblem problem;

	@Override
	public MartSeriesModel resume(MartSeriesModel martSeries) throws InterruptedException, ExecutionException {
		if (martSeries == null) 
			martSeries = new MartSeriesModel();

		CartLearner weakLearner = new CartLearner();
		weakLearner.setParams(params.cartParams);
		
		
		return martSeries;
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
