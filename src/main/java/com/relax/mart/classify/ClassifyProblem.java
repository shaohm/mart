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

import com.relax.lib.pcj.DoubleVector;
import com.relax.mart.Problem;

/**
 *
 * @author haimin.shao
 */
public interface ClassifyProblem extends Problem {
	
	/**
	 * compute total loss for predicts[start:end]
	 */
	double computeSessionLoss(DoubleVector predicts, ClassifyDataset dataset, int start, int end);

	/**
	 * the gradient of loss as a function of predicts[start:end]
	 */
	void computeSessionLossGradients(DoubleVector predicts, DoubleVector gradient, DoubleVector secondGradient, ClassifyDataset dataset, int start, int end);

	/**
	 * compute total readable loss for predicts[start:end]
	 */
	double computeReadableSessionLoss(DoubleVector predicts, ClassifyDataset dataset, int start, int end);
}
