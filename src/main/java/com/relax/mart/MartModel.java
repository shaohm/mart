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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author haimin.shao
 */
public class MartModel extends Model{

	public List<CartModel> cartModels = new ArrayList();

	public double predict(Instance instance) {
		double score = .0;
		for (CartModel cart : cartModels) {
			score += cart.predict(instance);
		}
		return score;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int m = 1; m <= this.cartModels.size(); m++) {
			buf.append("m=").append(m).append('\n');
			buf.append(cartModels.get(m - 1).toString()).append('\n');
		}
		return buf.toString();
	}

	public void fromString(String modelStr) {
		this.cartModels.clear();
		try (Scanner in = new Scanner(modelStr)) {
			in.useDelimiter("\n\n");
			if(! in.hasNext())
				return;
			while (in.hasNext()) {
				String cartStr = in.next();
				int i = cartStr.indexOf('\n');
				CartModel cart = new CartModel();
				cart.fromString(cartStr.substring(i + 1));
				this.cartModels.add(cart);
			}
		}
	}

	public RuleSetModel toRuleSetModel() {
		RuleSetModel ruleSetModel = new RuleSetModel();
		for (CartModel cart : this.cartModels) {
			for (CartModelNode node : cart.leaves) {
				ruleSetModel.rules.add(node.toRule());
			}
		}
		return ruleSetModel;
	}

	public static void dumpMarts(MartModel[] marts, File modelFile) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modelFile), "utf-8"))) {
			for(int k = 0; k < marts.length; k++) {
				MartModel mart = marts[k];
				String headerStr = String.format("t=%d", k);
				String modelStr = mart.toString();
				writer.write(headerStr, 0, headerStr.length());
				writer.write(modelStr, 0, modelStr.length());
			}
		}
	}
	
	public MartModel[] loadMarts(File modelFile) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		List<MartModel> marts = new ArrayList();
		StringBuilder buf = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile), "utf-8"))) {
			String line = reader.readLine();
			do {
				if(line == null || line.startsWith("t=")) {
					if(buf.length() > 0) {
						MartModel mart = new MartModel();
						mart.fromString(buf.toString());
						marts.add(mart);
						buf.setLength(0);
					}
					if(line == null) {
						break;
					} else {
						if(! line.equals(String.format("t=%d", marts.size())))
							throw new IllegalArgumentException("Bad target line: " + line);
						line = reader.readLine();
						continue;
					} 
				} 
				
				buf.append(line).append('\n');
				line = reader.readLine();
			} while(true);
		}
		return marts.toArray(new MartModel[marts.size()]);
	}

	@Override
	public double predict(Instance instance, int targetNo) {
		return this.predict(instance);
	}
}
