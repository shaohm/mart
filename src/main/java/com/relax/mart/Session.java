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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author haimin.shao
 */
public class Session {

	public int offset;
	public String id;
	public List<Instance> instances;
	public DoubleVector targets;

	public Session(int offset, String id) {
		this.offset = offset;
		this.id = id;
		this.instances = new ArrayList<Instance>();
		this.targets = new DoubleVector();
	}

	public void addExample(Instance instance, double target) {
		this.instances.add(instance);
		this.targets.append(target);
	}

	public void orderByTargetDesc() {
		qsort(this.targets, 0, this.targets.size(), this.instances, 0);
		for (int i = 0; i < this.instances.size(); i++) {
			this.instances.get(i).offset = i;
		}
	}

	public static void main(String args[]) {
		double value[] = {0, 0, 0, 1, 1, 1, 1, 1};
		DoubleVector targets = new DoubleVector(value);
		List<String> instances = Arrays.asList("e", "c", "a", "b", "f", "g", "d", "h");
		int from = 0, to = value.length;
		qsort(targets, from, to, instances, 0);
		System.out.println(targets);
		System.out.println(instances);
	}

	private static <T> void qsort(DoubleVector targets, int from, int to, List<T> instances, int level) {
		if (from >= to) {
			return;
		}
		// 检查是否已经有序，避免stack overflow的情况
		boolean inOrder = true;
		for(int i = from; i < to - 1; i++) {
			if(targets.get(i) < targets.get(i + 1)) {
				inOrder = false;
				break;
			}
		}
		if(inOrder) {
			return;
		}
		
		// 选取支点
		double pivot = targets.get(from);
		T pivotInstance = instances.get(from);
		
		// 检查支点性质，平衡区域划分，减少stack overflow的可能性（在二值标签情况下极易发生的情况）
		int numSmallers = 0, numBiggers = 0;
		for(int i = from; i < to; i++) {
			if(targets.get(i) > pivot)
				numBiggers ++;
			else
				numSmallers ++;
 		}
		
		int i = from, j = to - 1;
		while (true) {
			while (i < j && (targets.get(j) < pivot  || targets.get(j) == pivot && numBiggers > numSmallers )) {
				j--;
			}
			if (i == j) {
				break;
			}
			targets.set(targets.get(j), i);
			instances.set(i, instances.get(j));
			i++;

			while (i < j && (targets.get(i) > pivot || targets.get(i) == pivot && numBiggers <= numSmallers)) {
				i++;
			}
			if (i == j) {
				break;
			}
			targets.set(targets.get(i), j);
			instances.set(j, instances.get(i));
			j--;
		}
		targets.set(pivot, i);
		instances.set(i, pivotInstance);
		qsort(targets, from, i, instances, level + 1);
		qsort(targets, i + 1, to, instances, level + 1);
	}
}
