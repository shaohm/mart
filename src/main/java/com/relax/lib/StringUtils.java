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
package com.relax.lib;

/**
 *
 * @author haimin.shao
 */
public class StringUtils {
	public static int indexOf(String delimeters, String line) {
		return indexOf(delimeters, line, 0, line.length());
	}

	public static int indexOf(String delimeters, String line, int from) {
		return indexOf(delimeters, line, from, line.length());
	}

	public static int indexOf(String delimeters, String line, int from, int to) {
		for (int i = from; i < to; i++) {
			if (delimeters.indexOf(line.charAt(i)) >= 0) {
				return i;
			}
		}
		return -1;
	}
}
