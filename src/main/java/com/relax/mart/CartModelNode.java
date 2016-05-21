/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Scanner;

/**
 *
 * @author haimin.shao
 */
public class CartModelNode {

    public int seq; // 满二叉树的编号
    public CartModelNode parent;
    public CartModelNode left;
    public CartModelNode right;
    public int numInstances;
    public double error;
    public double predict;
    public int splitFeature;
    public double splitValue;
    public double splitGain;

    public CartModelNode() {
    }
    
    public CartModelNode(CartModelNode node) {
        this.seq = node.seq;
        this.parent = node.parent;
        this.left = node.left;
        this.right = node.right;
        this.numInstances = node.numInstances;
        this.error = node.error;
        this.predict = node.predict;
        this.splitFeature = node.splitFeature;
        this.splitValue = node.splitValue;
        this.splitGain = node.splitGain;
    }
    
    
    public Rule toRule() {
        Rule rule = new Rule();
        rule.predict = this.predict;
        CartModelNode cartNode = this;
        while(cartNode.parent != null) {
            Rule.Node node = new Rule.Node();
            node.splitFeature = cartNode.parent.splitFeature;
            node.splitValue = cartNode.parent.splitValue;
            node.le = cartNode == cartNode.parent.left;
            rule.path.nodes.add(node);
            cartNode = cartNode.parent;
        }
        Collections.reverse(rule.path.nodes);
        return rule;
    }
    
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("seq=").append(String.format("%02d", seq)).append(", ");
        buf.append("p=").append(String.format("%02d", parent==null ? 0 : parent.seq)).append(", ");
        buf.append("l=").append(String.format("%02d", left==null ? 0 : left.seq)).append(", ");
        buf.append("r=").append(String.format("%02d", right==null ? 0 : right.seq)).append(", ");
        buf.append("f=").append(String.format("%03d",splitFeature)).append(", ");
        buf.append("v=").append(String.format("%05.3f", splitValue)).append(", ");
        buf.append("pdt=").append(String.format("%+8.6f", predict)).append(", ");
        buf.append("nis=").append(String.format("%04d", numInstances)).append(", ");
        buf.append("err=").append(String.format("%010.4f", error)).append(", ");
        buf.append("gain=").append(String.format("%010.4f", splitGain));
        return buf.toString();
    }

    
    public void fromString(String nodeStr) {
        Scanner in = new Scanner(nodeStr);
        in.useDelimiter("[ =,]+");
        while (in.hasNext()) {
            String name = in.next();
            switch (name) {
                case "seq":
                    this.seq = in.nextInt();
                    break;
                case "p":
                    this.parent = new CartModelNode(); // a parent with seq=0 means no parent
                    this.parent.seq = in.nextInt();
                    break;
                case "l":
                    this.left = new CartModelNode(); // a child with seq=0 means no child
                    this.left.seq = in.nextInt();
                    break;
                case "r":
                    this.right = new CartModelNode();
                    this.right.seq = in.nextInt();
                    break;
                case "nis":
                    this.numInstances = in.nextInt();
                    break;
                case "err":
                    this.error = in.nextDouble();
                    break;
                case "pdt":
                    this.predict = in.nextDouble();
                    break;
                case "f":
                    this.splitFeature = in.nextInt();
                    break;
                case "v":
                    this.splitValue = in.nextDouble();
                    break;
                case "gain":
                    this.splitGain = in.nextDouble();
                    break;
                default:
                    throw new IllegalArgumentException("bad input: " + nodeStr);
            }
        }
    }

    public void print(PrintWriter writer) {
        if (this.seq > 1) {
            int level = (int) (Math.log(this.seq) / Math.log(2));
            for (int i = 1; i < level; i++) {
                writer.print("|    ");
            }
            writer.print("|----");
        }
        writer.printf("%d [num:%d, error:%g, guess:%g, split:%g@%d, gain:%g]\n", seq,
                this.numInstances, this.error, this.predict, this.splitValue, this.splitFeature, this.splitGain);
        if (this.left != null) {
            this.left.print(writer);
            this.right.print(writer);
        }
    }

}
