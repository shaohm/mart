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
package com.relax.lib.pcj;

import java.util.Arrays;

/**
 *
 * @author haimin.shao
 */
public class DoubleVector {

	private double[] value;
	private int size;

	public DoubleVector() {
		this(16);
	}

	public DoubleVector(int initCapacity) {
		this.value = new double[initCapacity];
		this.size = 0;
	}

	public DoubleVector(double[] value) {
		this.value = value;
		this.size = value.length;
	}

	public DoubleVector(double v, int size) {
		this.value = new double[size];
		for (int i = 0; i < size; i++) {
			this.value[i] = v;
		}
		this.size = size;
	}

	public DoubleVector(DoubleVector dv) {
		this.value = new double[dv.size];
		System.arraycopy(dv.value, 0, this.value, 0, dv.size);
		this.size = dv.size;
	}

	public void set(double d, int index) {
		if (index < 0 || index >= value.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (index >= size) {
			while (size <= index) {
				value[size++] = .0;
			}
		}
		value[index] = d;
	}

	public double get(int index) {
		if (index < 0 || index >= size) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return value[index];
	}

	public void append(double v) {
		if (this.value.length < this.size + 1) {
			double[] newValue = new double[this.size * 2];
			System.arraycopy(this.value, 0, newValue, 0, this.size);
			this.value = newValue;
		}
		this.value[this.size] = v;
		this.size += 1;
	}

	public void append(double v, int size) {
		if (this.value.length < this.size + size) {
			double[] newValue = new double[(this.size + size) * 2];
			System.arraycopy(this.value, 0, newValue, 0, this.size);
			this.value = newValue;
		}

		Arrays.fill(this.value, this.size, this.size + size, v);
		this.size += size;
	}
	
	public void append(double src[], int from, int end) {
		int srclen = end - from;
		if (this.value.length < this.size + srclen) {
			double[] newValue = new double[(this.size + srclen) * 2];
			System.arraycopy(this.value, 0, newValue, 0, this.size);
			this.value = newValue;
		}
		System.arraycopy(src, from, this.value, this.size, srclen);
		this.size += srclen;
	}

	public void append(DoubleVector slice) {
		if (this.value.length < this.size + slice.size) {
			double[] newValue = new double[(this.size + slice.size) * 2];
			System.arraycopy(this.value, 0, newValue, 0, this.size);
			this.value = newValue;
		}
		System.arraycopy(slice.value, 0, this.value, this.size, slice.size);
		this.size += slice.size;
	}

	public void clear(int from) {
		this.size = from;
	}

	/**
	 * @return the value
	 */
	public double[] backingArray() {
		return value;
	}

	/**
	 * @return the size
	 */
	public int size() {
		return size;
	}

	public int find(double v) {
		return Arrays.binarySearch(value, 0, size, v);
	}

	public DoubleVector sort() {
		Arrays.sort(value, 0, size);
		return this;
	}

	public DoubleVector uniq() {
		if (size == 0) {
			return this;
		}
		int i = 0, j = 1;
		while (j < size) {
			if (value[j] != value[i]) {
				i += 1;
				value[i] = value[j];
			}
			j += 1;
		}
		size = i + 1;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append('[');
		for (int i = 0; i < size; i++) {
			buf.append(value[i]).append(", ");
		}
		if (size > 0) {
			buf.setLength(buf.length() - 2);
		}
		buf.append(']');
		return buf.toString();
	}

}
