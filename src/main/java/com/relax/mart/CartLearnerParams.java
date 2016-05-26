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
public class CartLearnerParams {

	/**
	 * 回归树叶子节点的数量上限。
	 */
	public int maxNumLeaves = 6;

	/**
	 * 回归树的最大深度。只有根节点的树深度为0.
	 */
	public int maxDepth = 3;

	/**
	 * 每个叶子节点的样例数的最低值。
	 */
	public int minNumExamplesAtLeaf = 6;

	/**
	 * 分割的收益占当前误差的最低比例。低于此值，取消分割。
	 */
	public double minRatioSplitGain = 0.000001;
	
	/**
	 * 用于加速训练。 
	 * 分割时，如果样本量大于两倍的suitableNumExamplesForSplit，
	 * 就会随机选取suitableNumExamplesForSplit个样本来计算最佳分割点。
	 * 默认，使用全部样本。
	 */
	public int suitableNumExamplesForSplit = Integer.MAX_VALUE;

}
