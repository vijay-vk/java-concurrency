package org.learning.javaconcurrency.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomOperation {

	private static final Logger LOG = LoggerFactory.getLogger(RandomOperation.class);

	private static List<Integer> intList = new ArrayList<>();
	private static List<Long> longList = new ArrayList<>();

	static {

		// int-2500000 ; long-1250000 - 10 mb of shared data
		// int-1250000 ; long-625000 - 5 mb of shared data
		// int-125000 ; long-62500 - 0.5 mb of shared data

		String sharedMemory = System.getProperty("sharedMemory");
		LOG.info("sharedMemory size : " + sharedMemory);

		if ("10".equals(sharedMemory)) {
			for (int i = 2500000; i > 0; i--) {
				intList.add(i);
			}

			for (long i = 1250000; i > 0; i--) {
				longList.add(i);
			}
		} else if ("5".equals(sharedMemory)) {
			for (int i = 1250000; i > 0; i--) {
				intList.add(i);
			}

			for (long i = 625000; i > 0; i--) {
				longList.add(i);
			}
		} else {
			for (int i = 125000; i > 0; i--) {
				intList.add(i);
			}

			for (long i = 62500; i > 0; i--) {
				longList.add(i);
			}
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
		for (int i = 1000000; i > 0; i--) {
			int randomValue = ThreadLocalRandom.current().nextInt(10000001);
			list.add(randomValue);
		}

		return list;
	}

	public static List<Long> getNewLongList() {
		List<Long> list = new ArrayList<>();
		for (long i = 1000000; i > 0; i--) {
			long randomValue = ThreadLocalRandom.current().nextLong(10000001);
			list.add(randomValue);
		}

		return list;
	}
}
