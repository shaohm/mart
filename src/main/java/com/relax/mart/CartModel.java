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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 *
 * @author haimin.shao
 */
public class CartModel {

	CartModelNode root;
	List<CartModelNode> leaves;

	public CartModel() {
		this(null);
	}

	public CartModel(CartModelNode root) {
		this.root = root;
		this.leaves = new ArrayList();
		this.collectLeaves();
	}

	public void simplify() {
		root = new CartModelNode(root);
		Queue<CartModelNode> q = new LinkedList();
		q.add(root);
		while (!q.isEmpty()) {
			CartModelNode node = q.poll();
			if (node.left != null) {
				node.left = new CartModelNode(node.left);
				node.left.parent = node;
				node.right = new CartModelNode(node.right);
				node.right.parent = node;
				q.add(node.left);
				q.add(node.right);
			}
		}
		this.collectLeaves();
	}

	public List<Rule> toRuleSet() {
		List<Rule> rules = new ArrayList();
		for (CartModelNode node : leaves) {
			rules.add(node.toRule());
		}
		return rules;
	}

	private void collectLeaves() {
		if (this.root == null) {
			return;
		}
		this.leaves.clear();
		Queue<CartModelNode> q = new LinkedList();
		q.add(root);
		while (!q.isEmpty()) {
			CartModelNode node = q.poll();
			if (node.left == null) {
				this.leaves.add(node);
			} else {
				q.add(node.left);
				q.add(node.right);
			}
		}
	}

	public double predict(Instance instance) {
		CartModelNode child = root;
		CartModelNode parent = null;
		while (true) {
			if (child == null) {
				return parent.predict;
			}
			parent = child;
			double value = instance.findValue(parent.splitFeature);
			if (value <= parent.splitValue) {
				child = parent.left;
			} else {
				child = parent.right;
			}
		}
	}

	@Override
	public String toString() {
		if (root == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		Queue<CartModelNode> q = new LinkedList();
		q.add(root);
		while (!q.isEmpty()) {
			CartModelNode node = q.poll();
			buf.append(node.toString()).append("\n");
			if (node.left != null && node.right != null) {
				q.add(node.left);
				q.add(node.right);
			}
		}
		return buf.toString();
	}

	public void fromString(String modelStr) {
		if (modelStr == null || modelStr.isEmpty()) {
			this.root = null;
		} else {
			String[] lines = modelStr.split("\n");

			Map<Integer, CartModelNode> nodeMap = new TreeMap();
			nodeMap.put(0, null);
			for (String line : lines) {
				CartModelNode node = new CartModelNode();
				node.fromString(line);
				nodeMap.put(node.seq, node);
//                System.out.println("L:" + line);
			}
			for (CartModelNode node : nodeMap.values()) {
				if (node != null) {
					node.parent = nodeMap.get(node.parent.seq);
					node.left = nodeMap.get(node.left.seq);
					node.right = nodeMap.get(node.right.seq);
				}
			}
			this.root = nodeMap.get(1);
		}
		this.collectLeaves();;
	}

	public void print(PrintWriter writer) {
		this.root.print(writer);
	}

}
