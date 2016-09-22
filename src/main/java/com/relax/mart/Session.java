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
import com.relax.lib.pcj.Sorter;
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
//	public DoubleVector predicts;
//	public DoubleVector gradient;
//	public DoubleVector secondGradient;

	public Session(int offset, String id) {
		this.offset = offset;
		this.id = id;
		this.instances = new ArrayList<Instance>();
		this.targets = new DoubleVector();
//		this.predicts = new DoubleVector();
	}

	public void addExample(Instance instance, double target) {
		this.instances.add(instance);
		this.targets.append(target);
	}

	public void orderByTargetDesc() {
		Sorter.sort(targets.backingArray(), 0, targets.size(), instances, false);
		for (int i = 0; i < this.instances.size(); i++) {
			this.instances.get(i).offset = i;
		}
	}

	public static void main(String args[]) {
		double value[] = {0, 0, 0, 1, 1, 1, 1, 1};
		DoubleVector targets = new DoubleVector(value);
		List<String> instances = Arrays.asList("e", "c", "a", "b", "f", "g", "d", "h");
		int from = 0, to = value.length;
		Sorter.sort(targets.backingArray(), from, to, instances, false);
//		qsort(targets, from, to, instances, 0);
		System.out.println(targets);
		System.out.println(instances);
	}
}
