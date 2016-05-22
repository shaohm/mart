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
 * 对于list-wise和pair-wise，每个函数处理一个session。对于point-wise，每个函数处理一个数据集。
 *
 * @author haimin.shao
 */
public interface Problem {

	/**
	 * compute total loss for all predict
	 */
	double computeSessionLoss(DoubleVector predicts, Session session);

	/**
	 * the gradient of loss as a function of all predicts
	 */
	void computeSessionLossGradients(DoubleVector predicts, DoubleVector gradient, DoubleVector secondGradient, Session session);

	/**
	 * compute total loss for all predict
	 */
	double computeReadableSessionLoss(DoubleVector predicts, Session session);
}
