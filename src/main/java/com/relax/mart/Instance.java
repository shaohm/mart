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

import com.relax.lib.Pair;
import com.relax.lib.pcj.DoubleVector;
import com.relax.lib.pcj.IntVector;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author haimin.shao
 */
public class Instance {

	public int offset; // the offset of this instance in session
	public IntVector indices;
	public DoubleVector values;
	public List<Pair<String, String>> attributes;

	public static void main(String[] args) {
		String line = " 100:0.1   101:0.3  ###type:hotel   index:1 ";
		Instance instance = new Instance(0, line);
		System.out.println(instance);
	}

	public Instance(int offset, String line) {
		this.offset = offset;
		int p = line.indexOf('#');
		p = p < 0 ? line.length() : p;

		String[] slices = line.substring(0, p).trim().split("\\s+");
		indices = new IntVector(slices.length);
		values = new DoubleVector(slices.length);
		for (String slice : slices) {
			int j = slice.indexOf(':');
			int feature = Integer.parseInt(slice.substring(0, j));
			double value = Double.parseDouble(slice.substring(j + 1));
			indices.append(feature);
			values.append(value);
		}

		while (p < line.length() && line.charAt(p) == '#') {
			p++;
		}
		if (p < line.length()) {
			this.attributes = new ArrayList();
			slices = line.substring(p).trim().split("\\s+");
			for (String slice : slices) {
				int j = slice.indexOf(':');
				String name = slice.substring(0, j);
				String value = slice.substring(j + 1);
				this.attributes.add(new Pair<String, String>(name, value));
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < indices.size(); i++) {
			int feature = indices.get(i);
			double value = values.get(i);
			buf.append(String.format("%d:%.4f ", feature, value));
		}
		if (this.attributes != null) {
			buf.append('#');
			for (Pair<String, String> attr : this.attributes) {
				buf.append(' ').append(attr.first).append(':').append(attr.second);
			}
		}
		return buf.toString();
	}

	public String getAttribute(String name) {
		if (attributes == null) {
			return null;
		}
		for (Pair<String, String> p : attributes) {
			if (p.first.equals(name)) {
				return p.second;
			}
		}
		return null;
	}

	public int getSize() {
		return indices.size();
	}

	public int indexAt(int i) {
		return indices.get(i);
	}

	public double valueAt(int i) {
		return values.get(i);
	}

	public double findValue(int index) {
		int i = indices.find(index);
		if (i < 0) {
			return .0;
		}
		return valueAt(i);
	}

}
