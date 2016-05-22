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
import com.relax.lib.pcj.IntVector;

/**
 * 只允许整型的target，且target的值最低是0，且绝大多数实例是0.
 *
 * @author haimin.shao
 */
public class BipartiPairwiseRankProblem implements Problem {

	public double regularizationWeight = 0.0001;
	public int readableLossTopN = 2;

	@Override
	public double computeSessionLoss(DoubleVector predicts, Session session) {
		// 假设targets是按照分值从高到低排序的。（在构造训练集时指定即可）
		int numPositives = 0;
		while (numPositives < session.targets.size() && session.targets.get(numPositives) > 0) {
			numPositives += 1;
		}

		double loss = .0;
		for (int i = 0; i < numPositives; i++) {
			for (int j = numPositives; j < session.targets.size(); j++) {
				loss += Math.log(1 + Math.exp(predicts.get(j) - predicts.get(i)));
			}
		}
		for (int i = 0; i < predicts.size(); i++) {
			loss += regularizationWeight * 0.5 * predicts.get(i) * predicts.get(i);
		}
		return loss;
	}

	@Override
	public void computeSessionLossGradients(DoubleVector predicts, DoubleVector gradient, DoubleVector secondGradient, Session session) {

		gradient.clear(0);
		gradient.append(0.0, session.targets.size());
		secondGradient.clear(0);
		secondGradient.append(0.0, session.targets.size());

		int numPositives = 0;
		while (numPositives < session.targets.size() && session.targets.get(numPositives) > 0) {
			numPositives += 1;
		}

		for (int i = 0; i < numPositives; i++) {
			for (int j = numPositives; j < session.targets.size(); j++) {
				double exp = Math.exp(predicts.get(j) - predicts.get(i));
				double ngi = gradient.get(i) - exp / (1 + exp);
				double ngj = gradient.get(j) + exp / (1 + exp);
				gradient.set(ngi, i);
				gradient.set(ngj, j);
				double nsgi = secondGradient.get(i) + exp / (1 + exp) / (1 + exp);
				double nsgj = secondGradient.get(j) + exp / (1 + exp) / (1 + exp);
				secondGradient.set(nsgi, i);
				secondGradient.set(nsgj, j);
			}
		}

		// 由于cart本身有防止过拟合策略，且此处参数量大，半平方和损失太重，需要将规整化系数调低
		for (int i = 0; i < session.targets.size(); i++) {
			gradient.set(gradient.get(i) + regularizationWeight * predicts.get(i), i);
			secondGradient.set(secondGradient.get(i) + regularizationWeight, i);
		}
	}

	@Override
	public double computeReadableSessionLoss(DoubleVector predicts, Session session) {
		int numPositives = 0;
		while (numPositives < session.targets.size() && session.targets.get(numPositives) > 0) {
			numPositives += 1;
		}

		DoubleVector predictsBak = new DoubleVector(predicts);
		IntVector indices = new IntVector();
		for (int i = 0; i < predictsBak.size(); i++) {
			indices.append(i);
		}
		sortDesc(predictsBak, 0, predictsBak.size(), indices);

		IntVector counts = new IntVector();
		int from = 0, to = 0;
		while (from < predictsBak.size()) {
			int numLocalPositives = 0, numLocalNegatives = 0;
			while (to < predictsBak.size() && predictsBak.get(to) == predictsBak.get(from)) {
				if (indices.get(to) < numPositives) {
					numLocalPositives += 1;
				} else {
					numLocalNegatives += 1;
				}
				to++;
			}
			counts.append(numLocalPositives);
			counts.append(numLocalNegatives);
			from = to;
		}

		int numPassed = 0;
		for (int i = 0; i < counts.size(); i += 2) {
			if (numPassed >= this.readableLossTopN) {
				break;
			}
			int np = counts.get(i);
			int nn = counts.get(i + 1);
			if (this.readableLossTopN - numPassed > np + nn) {
				if (np > 0) {
					return 0.0;
				}
			} else if (this.readableLossTopN - numPassed < np + nn) {
				if (np == 0) {
					return 1.0;
				}
				if (nn < this.readableLossTopN - numPassed) {
					return 0.0;
				}

				double loss = 1.0;
				for (int j = 0; j < this.readableLossTopN - numPassed; j++) {
					loss *= ((nn - j) / (double) (np + nn - j));
				}
				return loss;
			} else {
				if (np > 0) {
					return 0.0;
				}
				return 1.0;
			}
			numPassed -= np + nn;
		}
		return .0;
	}

	private static <T> void sortDesc(DoubleVector predicts, int from, int to, IntVector indices) {
		if (from >= to) {
			return;
		}
		double pivot = predicts.get(from);
		int pivotIndex = indices.get(from);
		int i = from, j = to - 1;
		while (true) {
			while (i < j && predicts.get(j) <= pivot) {
				j--;
			}
			if (i == j) {
				break;
			}
			predicts.set(predicts.get(j), i);
			indices.set(indices.get(j), i);
			i++;

			while (i < j && predicts.get(i) >= pivot) {
				i++;
			}
			if (i == j) {
				break;
			}
			predicts.set(predicts.get(i), j);
			indices.set(indices.get(i), j);
			j--;
		}
		predicts.set(pivot, i);
		indices.set(pivotIndex, i);
		sortDesc(predicts, from, i, indices);
		sortDesc(predicts, i + 1, to, indices);
	}

	public static void main(String args[]) {
		Session session = new Session(0, "");
		session.targets = new DoubleVector(new double[]{1, 1, 0, 0, 0, 0, 0, 0});
		DoubleVector predicts = new DoubleVector(new double[]{2, 7, 3, 4, 5, 6, 3, 8});
//        DoubleVector predicts = new DoubleVector(new double[]{0,0,0,0,0,0,0,0});
		DoubleVector gradient = new DoubleVector();
		DoubleVector secondGradient = new DoubleVector();

		IntVector indices = new IntVector();
		indices.append(0);
		indices.append(1);
		BipartiPairwiseRankProblem problem = new BipartiPairwiseRankProblem();
		problem.readableLossTopN = 6;
		problem.regularizationWeight = 0.000001;
		System.out.println(problem.computeSessionLoss(predicts, session));
		System.out.println(problem.computeReadableSessionLoss(predicts, session));
		problem.computeSessionLossGradients(predicts, gradient, secondGradient, session);
		System.out.println(gradient);
		System.out.println(secondGradient);
	}
}
