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

/**
 * target is +1 for positive example, -1 for negative example
 *
 * @author haimin.shao
 */
public class BinaryClassificationProblem implements Problem {

	double regularizationWeight = 0.0001;
	double positiveExampleWeight = 1.0;
	double negativeExampleWeight = 1.0;

	@Override
	public double computeSessionLoss(DoubleVector predicts, Session session) {
		double loss = .0;
		for (int i = 0; i < session.targets.size(); i++) {
			double t = session.targets.get(i);
			double p = predicts.get(i);
			double w = (t > 0) ? positiveExampleWeight : negativeExampleWeight;
			loss += w * Math.log(1 + Math.exp(-p * t));
			loss += regularizationWeight * 0.5 * p * p;
		}
		return loss;
	}

	@Override
	public void computeSessionLossGradients(DoubleVector predicts, DoubleVector gradient, DoubleVector secondGradient, Session session) {
		gradient.clear(0);
		gradient.append(0.0, session.targets.size());
		secondGradient.clear(0);
		secondGradient.append(0.0, session.targets.size());

		for (int i = 0; i < session.targets.size(); i++) {
			double t = session.targets.get(i);
			double p = predicts.get(i);
			double w = (t > 0) ? positiveExampleWeight : negativeExampleWeight;
			double exp = Math.exp(-t * p);
			gradient.set(w * -t * exp / (1 + exp) + regularizationWeight * p, i);
			secondGradient.set(w * exp / (1 + exp) / (1 + exp) + regularizationWeight, i);
		}
	}

	/**
	 * 简单计算精度吧。
	 */
	@Override
	public double computeReadableSessionLoss(DoubleVector predicts, Session session) {
		double loss = .0;
		for (int i = 0; i < session.targets.size(); i++) {
			double t = session.targets.get(i);
			double p = predicts.get(i);
			if (p == 0) {
				loss += 0.5;
			}
			if (t < 0 && p > 0 || t > 0 && p < 0) {
				loss += 1;
			}
		}
		loss /= session.targets.size();
		return loss;
	}

}
