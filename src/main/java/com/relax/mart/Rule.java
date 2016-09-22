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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author haimin.shao
 */
public class Rule {

	public double predict = .0;
	public Path path = new Path();

	@Override
	public String toString() {
		return String.format("%g %s", predict, path.toString());
	}

	public void fromString(String ruleStr) {
		String slices[] = ruleStr.split("\\s+", 2);
		predict = Double.parseDouble(slices[0].trim());
		path = new Path();
		path.fromString(slices[1].trim());
	}

	public static interface Condition {
		boolean accept(Instance instance);
	}

	public static class Node implements Condition, Comparable<Node> {

		int splitFeature;
		double splitValue;
		boolean le;

		@Override
		public boolean accept(Instance instance) {
			double v = instance.findValue(splitFeature);
			return v <= splitValue && le || v > splitValue && !le;
		}

		@Override
		public String toString() {
			return String.format("f%d %s %f", splitFeature, le ? "<=" : ">", splitValue);
		}

		public void fromString(String nodeStr) {
			String[] slices = nodeStr.trim().split("\\s+");
			if (slices.length != 3) {
				throw new IllegalArgumentException(nodeStr);
			}
			splitFeature = Integer.parseInt(slices[0].substring(1));
			le = "<".equals(slices[1]);
			splitValue = Double.parseDouble(slices[2]);
		}

		@Override
		public int compareTo(Node o) {
			int r;
			r = Integer.compare(this.splitFeature, o.splitFeature);
			if(r != 0)
				return r;
			r = Double.compare(this.splitValue, o.splitValue);
			if(r != 0)
				return r;
			return le ? -1 : 1;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 13 * hash + this.splitFeature;
			hash = 13 * hash + (int) (Double.doubleToLongBits(this.splitValue) ^ (Double.doubleToLongBits(this.splitValue) >>> 32));
			hash = 13 * hash + (this.le ? 1 : 0);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Node other = (Node) obj;
			if (this.splitFeature != other.splitFeature) {
				return false;
			}
			if (Double.doubleToLongBits(this.splitValue) != Double.doubleToLongBits(other.splitValue)) {
				return false;
			}
			if (this.le != other.le) {
				return false;
			}
			return true;
		}

	}

	public static class Path implements Condition, Comparable<Path> {

		List<Node> nodes = new ArrayList(4);
		
		@Override
		public boolean accept(Instance instance) {
			for (Node node : nodes) {
				if (!node.accept(instance)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				buf.append(node);
				if (i < nodes.size() - 1) {
					buf.append(" & ");
				}
			}
			return buf.toString();
		}

		private void fromString(String pathStr) {
			nodes.clear();
			String[] slices = pathStr.trim().split("&");
			for (String nodeStr : slices) {
				Node node = new Node();
				node.fromString(nodeStr.trim());
				nodes.add(node);
			}
		}

		@Override
		public int compareTo(Path o) {
			for(int i = 0; i < Math.min(nodes.size(), o.nodes.size()); i++) {
				int r = nodes.get(i).compareTo(o.nodes.get(i));
				if(r != 0)
					return r;
			}
			return nodes.size() - o.nodes.size();
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 47 * hash + Objects.hashCode(this.nodes);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Path other = (Path) obj;
			if (!Objects.equals(this.nodes, other.nodes)) {
				return false;
			}
			return true;
		}
		
		
	}
}
