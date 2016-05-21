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
public class IntVector {

	private int[] value;
	private int size;

	public IntVector() {
		this(16);
	}

	public IntVector(int initCapacity) {
		this.value = new int[initCapacity];
		this.size = 0;
	}

	public IntVector(int[] value) {
		this.value = value;
		this.size = value.length;
	}

	public IntVector(int v, int size) {
		this.value = new int[size];
		for (int i = 0; i < size; i++) {
			this.value[i] = v;
		}
		this.size = size;
	}

	public IntVector(IntVector iv) {
		this.value = new int[iv.size];
		System.arraycopy(iv.value, 0, this.value, 0, iv.size);
		this.size = iv.size;
	}

	public void set(int v, int index) {
		if (index < 0 || index >= value.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (index >= size) {
			while (size <= index) {
				value[size++] = 0;
			}
		}
		value[index] = v;
	}

	public int get(int index) {
		if (index < 0 || index >= size) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return value[index];
	}

	public void append(int v) {
		if (this.value.length < this.size + 1) {
			int[] newValue = new int[this.size * 2];
			System.arraycopy(this.value, 0, newValue, 0, this.size);
			this.value = newValue;
		}
		this.value[this.size] = v;
		this.size += 1;
	}

	public void append(int v, int size) {
		if (this.value.length < this.size + size) {
			int[] newValue = new int[(this.size + size) * 2];
			System.arraycopy(this.value, 0, newValue, 0, this.size);
			this.value = newValue;
		}

		Arrays.fill(this.value, this.size, this.size + size, v);
		this.size += size;
	}

	public void append(IntVector slice) {
		if (this.value.length < this.size + slice.size) {
			int[] newValue = new int[(this.size + slice.size) * 2];
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
	public int[] backingArray() {
		return value;
	}

	/**
	 * @return the size
	 */
	public int size() {
		return size;
	}

	public int find(int v) {
		return Arrays.binarySearch(value, 0, size, v);
	}

	public IntVector sort() {
		Arrays.sort(value, 0, size);
		return this;
	}

	public IntVector uniq() {
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
