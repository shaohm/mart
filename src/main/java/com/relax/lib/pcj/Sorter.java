/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.relax.lib.pcj;

import java.util.Arrays;
import java.util.List;

/**
 * 为保证效率，定制的排序函数。
 * @author haimin.shao
 */
public class Sorter {
	
	public static void sort(double[] arr, int from, int to, int[] ass, boolean isAsc) {
		if(to - from <= 10) {
			// use insertion sort for small subarray
			double t0;
			int t1;
			for(int i = from + 1; i < to; i++) {
				int j = i - 1;
				while(j >= from && (isAsc ? arr[j] > arr[i] : arr[j] < arr[i]))
					j --;
				if(i == j + 1)
					continue;
				// maybe System.arrayCopy can do it better
				t0 = arr[i];
				t1 = ass[i];
				for(int k = i - 1; k > j; k--) {
					arr[k+1] = arr[k];
					ass[k+1] = ass[k];
				}
				arr[j + 1] = t0;
				ass[j + 1] = t1;
			}
			return;
		}
		
		int a, b, c, d, n,s;
		double v, t0;
		int t1;
		n = to - from;
		v = arr[from + (int) (Math.random() * n)];
		a = b = from;
		c = d = to - 1;
//		System.out.printf("B: %03d-%03d %3.1f \t%s\n", from, to, v, Arrays.toString(arr));
		while(true) {
			while(b <= c && (isAsc ? arr[b] <= v : arr[b] >= v)) {
				if(arr[b] == v) {
					t0 = arr[b];
					arr[b] = arr[a];
					arr[a] = t0;
					
					t1 = ass[b];
					ass[b] = ass[a];
					ass[a] = t1;
					
					a ++;
				}
				b ++;
			}
			while(c >= b && (isAsc ? arr[c] >= v : arr[c] <= v)) {
				if(arr[c] == v) {
					t0 = arr[c];
					arr[c] = arr[d];
					arr[d] = t0;
					
					t1 = ass[c];
					ass[c] = ass[d];
					ass[d] = t1;
					
					d --;
				}
				c --;
			}
			if(b > c) break;
			
			t0 = arr[c];
			arr[c] = arr[b];
			arr[b] = t0;
			
			t1 = ass[c];
			ass[c] = ass[b];
			ass[b] = t1;
			
			b++; 
			c--;
		}
//		System.out.printf("I: %03d-%03d %3.1f \t%s\n", from, to, v, Arrays.toString(arr));
		s = a - from < b - a ? a - from : b - a;
		for(int l = from, h = b - s; s > 0; s--, l++, h++) {
			t0 = arr[l];
			arr[l] = arr[h];
			arr[h] = t0;
			
			t1 = ass[l];
			ass[l] = ass[h];
			ass[h] = t1;
		}
		
		s = d - c < to - 1 - d ? d - c : to - 1 - d;
		for(int l = c + 1, h = to - s; s > 0; s--, l++, h++) {
			t0 = arr[l];
			arr[l] = arr[h];
			arr[h] = t0;
			
			t1 = ass[l];
			ass[l] = ass[h];
			ass[h] = t1;
		}
//		System.out.printf("A: %03d-%03d %3.1f \t%s\n", from, to, v, Arrays.toString(arr));
		sort(arr, from, from + b - a, ass, isAsc);
		sort(arr, to - d + c, to, ass, isAsc);
//		System.out.printf("F: %03d-%03d %3.1f \t%s\n", from, to, v, Arrays.toString(arr));
	}
	
	public static <T> void sort(double[] arr, int from, int to, List<T> ass, boolean isAsc) {
		int[] src = new int[ass.size()];
		for(int i = from; i < to; i++)
			src[i] = i;
		sort(arr, from, to, src, isAsc);
		for(int i = from; i < to; i++) {
			if(src[i] == i) continue;
			T t = ass.get(i);
			int d = i, s = src[d];
			do {
				ass.set(d, ass.get(s));
				src[d] = d;
				d = s;
				s = src[d];
			} while(s != i);
			ass.set(d, t);
			src[d] = d;
		}
	}	
	
	public static <T> void sort(double[] arr, int from, int to, T[] ass, boolean isAsc) {
		int[] src = new int[ass.length];
		for(int i = from; i < to; i++)
			src[i] = i;
		sort(arr, from, to, src, isAsc);
		for(int i = from; i < to; i++) {
			if(src[i] == i) continue;
			T t = ass[i];
			int d = i, s = src[d];
			do {
				ass[d] = ass[s];
				src[d] = d;
				d = s;
				s = src[d];
			} while(s != i);
			ass[d] = t;
			src[d] = d;
		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		int n = (int)(Math.random() * 30 + 1);
		double[] arr = new double[n];
//		int[] ass = new int[n];
		Integer[] ass = new Integer[n];
		for(int i = 0; i < n; i ++) {
			arr[i] = (int)(Math.random() * 10);
			ass[i] = i;
		}
		System.out.println(Arrays.toString(arr));
		System.out.println(Arrays.toString(ass));
		sort(arr, 0, n, Arrays.asList(ass), false);
		System.out.println(Arrays.toString(arr));
		System.out.println(Arrays.toString(ass));
	}
}
