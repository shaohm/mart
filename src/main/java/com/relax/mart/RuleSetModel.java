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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 *
 * @author haimin.shao
 */
public class RuleSetModel {

	public double learningRate = 0.2;
	public List<Rule> rules = new ArrayList();

	public double predict(Instance instance) {
		double score = .0;
		for(Rule rule : rules) {
			if(rule.path.accept(instance))
				score += rule.predict;
		}
		score *= learningRate;
		return score;
	}

	public RuleSetModel reduced() {
		RuleSetModel m = new RuleSetModel();
		m.learningRate = 1;
		Map<Rule.Path, List<Rule>> clusters = new TreeMap();
		for(Map.Entry<Rule.Path, List<Rule>> ent : clusters.entrySet()) {
			Rule combined = new Rule();
			combined.path = ent.getKey();
			combined.predict = .0;
			for(Rule rule : ent.getValue()) {
				combined.predict += rule.predict;
			}
			combined.predict *= this.learningRate;
			m.rules.add(combined);
		}
		return m;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(this.learningRate).append('\n');
		for (int m = 1; m <= this.rules.size(); m++) {
			buf.append(rules.get(m - 1).toString()).append('\n');
		}
		return buf.toString();
	}

	public void fromString(String modelStr) {
		try (Scanner in = new Scanner(modelStr)) {
			if(in.hasNextLine())
				this.learningRate = Double.parseDouble(in.nextLine());
			while (in.hasNextLine()) {
				String ruleStr = in.nextLine();
				Rule rule = new Rule();
				rule.fromString(ruleStr);
				this.rules.add(rule);
			}
		}
	}

	public void dump(File rulesFile) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rulesFile), "utf-8"))) {
			writer.write(String.format("%g\n", this.learningRate));
			for (Rule rule : rules) {
				String ruleStr = rule.toString();
				writer.write(ruleStr, 0, ruleStr.length());
				writer.write('\n');
			}
		}
	}

	public void load(File rulesFile) throws IOException {
		StringBuilder buf = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(rulesFile), "utf-8"))) {
			while (true) {
				int c = reader.read();
				if (c > 0) {
					buf.append((char) c);
				} else {
					break;
				}
			}
		}
		String modelStr = buf.toString();
		this.fromString(modelStr);
	}
}
