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

/**
 *
 * @author haimin.shao
 */
public class MartLearnerParams {
	/** 
	 * 用于下层回归树训练的参数。
	 */
	public CartLearnerParams cartParams = new CartLearnerParams();
	/**
	 * 回归树的个数上限。
	 */
	public int numCarts = 100;
	/**
	 * 学习率。据传较低的learningRate会有较好的效果。
	 */
	public double learningRate = 0.3;
}
