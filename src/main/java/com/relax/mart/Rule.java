/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

import java.util.ArrayList;
import java.util.List;

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
        int i = ruleStr.indexOf(' ');
        predict = Double.parseDouble(ruleStr.substring(0, i).trim());
        path = new Path();
        path.fromString(ruleStr.substring(i + 1).trim());
    }
    
    public static interface Condition {
        boolean accept(Instance instance);
    }
    
    public static class Node implements Condition{
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
            if(slices.length != 3)
                throw new IllegalArgumentException(nodeStr);
            splitFeature = Integer.parseInt(slices[0].substring(1));
            le = "<".equals(slices[1]);
            splitValue = Double.parseDouble(slices[2]);
        }
        
    }
    
    public static class Path implements Condition {
        List<Node> nodes = new ArrayList(4);
        
        @Override
        public boolean accept(Instance instance) {
            for(Node node : nodes) {
                if(! node.accept(instance))
                    return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            for(int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                buf.append(node);
                if(i < nodes.size() - 1)
                    buf.append(" & ");
            }
            return buf.toString();
        }

        private void fromString(String pathStr) {
            nodes.clear();
            String[] slices = pathStr.trim().split("\\s*&\\s*");
            for(String nodeStr : slices) {
                Node node = new Node();
                node.fromString(nodeStr.trim());
                nodes.add(node);
            }
        }
    }
}
