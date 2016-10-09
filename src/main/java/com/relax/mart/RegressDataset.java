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

import com.relax.lib.StringUtils;
import com.relax.lib.pcj.DoubleVector;
import com.relax.lib.pcj.Sorter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author haimin.shao
 */
public class RegressDataset implements Dataset {
	
	public List<Instance> instances;
	public DoubleVector targets;

	public RegressDataset(List<Instance> instances, DoubleVector targets) {
		this.instances = instances;
		this.targets = targets;
	}
	
	public RegressDataset() {
		this.instances = new ArrayList<Instance>();
		this.targets = new DoubleVector();
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
	@Override
	public void load(File dataFile) throws FileNotFoundException {
		try (Scanner in = new Scanner(dataFile, "utf-8")) {
			while (in.hasNextLine()) {
				String line = in.nextLine();
				int i;
				i = StringUtils.indexOf(" \t", line);
				double target = Double.parseDouble(line.substring(0, i));
				line = line.substring(i + 1);
				
				
				Instance instance = new Instance(instances.size(), line);
				addExample(instance, target);
			}
		}
		orderByTargetDesc();
	}

	@Override
	public void dump(File dataFile) throws FileNotFoundException, UnsupportedEncodingException {
		try (PrintWriter out = new PrintWriter(dataFile, "utf-8")) {
			for (int i = 0; i < instances.size(); i++) {
				Instance instance = instances.get(i);
				double target = targets.get(i);
				out.print(target);
				out.print(" ");
				out.print(instance);
				out.println();
			}
			out.flush();
		}
	}

	public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {
		File fromFile = new File("D:\\SoftwareData\\rhcygwin64\\home\\haimin.shao\\code\\rankboost_code\\w.dat");
		File toFile = new File("D:\\SoftwareData\\rhcygwin64\\home\\haimin.shao\\code\\rankboost_code\\v.dat");
		RegressDataset ds = new RegressDataset();
		ds.load(fromFile);
		ds.dump(toFile);
	}
}
