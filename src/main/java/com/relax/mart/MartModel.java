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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author haimin.shao
 */
public class MartModel {
    public double learningRate;
    public List<CartModel> cartModels;

    public MartModel() {
        this(.0);
    }
    
    public MartModel(double learningRate) {
        this.learningRate = learningRate;
        this.cartModels = new ArrayList();
    }
   
   public double predict(Instance instance) {
       double score = .0;
        for (CartModel cart : cartModels) {
            score += learningRate * cart.predict(instance);
        }
       return score;
   } 

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.learningRate).append('\n').append('\n');
        for(int m = 1; m <= this.cartModels.size(); m++) {
            buf.append("m=").append(m).append('\n');
            buf.append(cartModels.get(m-1).toString()).append('\n');
        }
        return buf.toString();
    }
   
    public void fromString(String modelStr) {
        try (Scanner in = new Scanner(modelStr)) {
            in.useDelimiter("\n\n");
            this.learningRate = in.nextDouble();
            while(in.hasNext()) {
                String cartStr = in.next();
                int i = cartStr.indexOf('\n');
                CartModel cart = new CartModel();
                cart.fromString(cartStr.substring(i+1));
                this.cartModels.add(cart);
            }
        }
    }
    
    public void load(File modelFile) throws UnsupportedEncodingException, FileNotFoundException, IOException {
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
   
    public void dump(File modelFile) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modelFile), "utf-8"))) {
            String modelStr = this.toString();
            writer.write(modelStr, 0, modelStr.length());
        }
    }
    
    public RuleSetModel toRuleSetModel() {
        RuleSetModel ruleSetModel = new RuleSetModel();
        ruleSetModel.learningRate = this.learningRate;
        for(CartModel cart : this.cartModels) {
            for(CartModelNode node : cart.leaves) {
                ruleSetModel.rules.add(node.toRule());
            }
        }
        return ruleSetModel;
    }
    
}
