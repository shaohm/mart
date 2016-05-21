/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.mart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author haimin.shao
 */
public class RuleSetModel {
   public double learningRate = 0.2;
   public List<Rule> rules = new ArrayList();
   
   public double predict(Instance instance) {
       return .0;
   }
   
   @Override
   public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.learningRate).append('\n');
        for(int m = 1; m <= this.rules.size(); m++) {
            buf.append(rules.get(m-1).toString()).append('\n');
        }
        return buf.toString();
   }
   
   public void fromString(String modelStr) {
       try (Scanner in = new Scanner(modelStr)) {
           this.learningRate = in.nextDouble();
           while(in.hasNextLine()) {
               String ruleStr = in.next();
               Rule rule = new Rule();
               rule.fromString(ruleStr);
               this.rules.add(rule);
           }
       }
   }
   
   public void dump(File ruleSetFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruleSetFile), "utf-8"))) {
            writer.write(String.format("%g\n", this.learningRate));
            for(Rule rule : rules) {
                String ruleStr = rule.toString();
                writer.write(ruleStr, 0, ruleStr.length());
                writer.write('\n');
            }
        }
   }
   
   public void load(File modelFile) throws IOException {
        StringBuilder buf = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile), "utf-8"))) {
            while(true) {
                int c = reader.read();
                if(c > 0) {
                    buf.append((char)c);
                } else {
                    break;
                }
            }
        }
        String modelStr = buf.toString();
        this.fromString(modelStr);
   }
}
