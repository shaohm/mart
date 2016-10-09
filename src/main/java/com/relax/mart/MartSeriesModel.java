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
public class MartSeriesModel extends Model {
	
	public MartModel[] martModels;

	@Override
	public double predict(Instance instance, int targetNo) {
		return martModels[targetNo].predict(instance);
	}

	@Override
	public void fromString(String modelStr) {
		int i = 0, j = -1;
		String line;
		j = modelStr.indexOf('\n', i);
		line = modelStr.substring(i, j);
		if(!line.startsWith("numTargets=")) {
			throw new IllegalArgumentException("bad format");
		} else {
			int numTargets = Integer.parseInt(line.substring("numTargets=".length()));
			this.martModels = new MartModel[numTargets];
			
			for(int k = 0; k < numTargets; k++) {
				i = j + 1;
				j = modelStr.indexOf("\nt=", i);
				if(j < 0)
					j = modelStr.length();
				int l = modelStr.indexOf('\n', i);
				int targetNo = Integer.parseInt(modelStr.substring(i + "t=".length(), l));
				if(targetNo != k)
					throw new IllegalArgumentException("bad model format");
				this.martModels[targetNo] = new MartModel();
				this.martModels[targetNo].fromString(modelStr.substring(l+1, j));
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("numTargets=").append(martModels.length).append('\n');
		for(int targetNo = 0; targetNo < martModels.length; targetNo++) {
			buf.append("t=").append(targetNo).append('\n');
			buf.append(martModels[targetNo].toString()).append('\n');
		}
		return buf.toString();
	}
}
