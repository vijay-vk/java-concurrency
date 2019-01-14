package org.learning.javaconcurrency.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RandomOperation {

	public static void sortInt() {
		List<Integer> list = new ArrayList<>();
		for (int i = 10000000; i > 0; i--) {
			list.add(i);
		}
		list.sort(Comparator.naturalOrder());
	}
	
	public static void sortLong() {
		List<Long> list = new ArrayList<>();
		for (long i = 10000000; i > 0; i--) {
			list.add(i);
		}
		list.sort(Comparator.naturalOrder());
	}
}
