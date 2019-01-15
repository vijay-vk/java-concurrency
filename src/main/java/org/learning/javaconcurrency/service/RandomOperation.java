package org.learning.javaconcurrency.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomOperation {

	private static List<Integer> intList = new ArrayList<>();
	private static List<Long> longList = new ArrayList<>();

	static {
		for (int i = 10000000; i > 0; i--) {
			intList.add(i);
		}

		for (long i = 10000000; i > 0; i--) {
			longList.add(i);
		}
	}

	public static List<Integer> getIntList() {
		return intList;
	}

	public static List<Long> getLongList() {
		return longList;
	}

	public static List<Integer> getNewIntList() {
		List<Integer> list = new ArrayList<>();
		for (int i = 10000000; i > 0; i--) {
			int randomValue = ThreadLocalRandom.current().nextInt(10000001);
			list.add(randomValue);
		}

		return list;
	}

	public static List<Long> getNewLongList() {
		List<Long> list = new ArrayList<>();
		for (long i = 10000000; i > 0; i--) {
			long randomValue = ThreadLocalRandom.current().nextLong(10000001);
			list.add(randomValue);
		}

		return list;
	}
}
