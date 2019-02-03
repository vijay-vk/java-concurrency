### Cache Miss - Example in Java
* For Applications which deals with parallel tasks, if the shared memory between a Producer and a Consumer crosses ~8MB, it can impact the performance of an Application due to the cache-miss effect.
* This scenario can happen for use cases like rule engine - where all incoming requests are validated against pre-defined rules and these rules can grow over time crossing the 8 MB limit.
* When the shared memory between threads crosses ~8 MB, handling in-memory tasks with single thread had much better performance than handling tasks with multiple threads.
* The size (~8 MB) can change based on the processor

#### Cache Miss
* CPU architecture has multiple layers of cache before fetching any data/object from RAM memory.
* To map this with JVM memory model, all the objects in Heap memory will reside in RAM.
* If CPU has to execute the instructions, it has to fetch those data into L1, L2 cache and eventually into CPU registers to execute it.
* In concurrent use cases, if same data is accessed by both the core simultaneously, then cache memory will not be used effectively.

![Cache-Miss](/images/cache-miss.png)

#### Example of cache-miss

```java
public class CacheMiss {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		List<Integer> intList = new ArrayList<>();
		for (int i = 0; i < 10000000; i++) {
			intList.add(i);
		}
		System.out.println("Main Thread : " + Thread.currentThread().getName());

		doSomeRandomOperationInList(intList);
		doSomeRandomOperationInList(intList);
		doSomeRandomOperationInList(intList);
		doSomeRandomOperationInList(intList);
		doSomeRandomOperationInList(intList);

		runAsync(intList, 1);
		runAsync(intList, 2);
		runAsync(intList, 3);
		runAsync(intList, 4);
		runAsync(intList, 5);

		TimeUnit.MINUTES.sleep(1);

	}

	private static void runAsync(List<Integer> intList, int t) {
		CompletableFuture.runAsync(() -> {
			System.out.println("new thread - " + t + " : " + Thread.currentThread().getName());
			long s = System.currentTimeMillis();
			List<Integer> l = intList.stream().map(i -> i * 2).collect(Collectors.toList());
			long e = System.currentTimeMillis();
			System.out.println("Thread : " + t + " : " + (e - s));
		});
	}

	private static void doSomeRandomOperationInList(List<Integer> intList) {
		long startTime = System.currentTimeMillis();
		intList.stream().map(i -> i * 2).collect(Collectors.toList());
		long endTime = System.currentTimeMillis();
		System.out.println(
				"Thread : " + Thread.currentThread().getName() + " : Time Taken in (ms) : " + (endTime - startTime));
	}

}
// Output
// Main Thread : main
// Thread : main : Time Taken in (ms) : 1838
// Thread : main : Time Taken in (ms) : 490
// Thread : main : Time Taken in (ms) : 542
// Thread : main : Time Taken in (ms) : 322
// Thread : main : Time Taken in (ms) : 325
// new thread - 1 : ForkJoinPool.commonPool-worker-1
// new thread - 3 : ForkJoinPool.commonPool-worker-3
// new thread - 2 : ForkJoinPool.commonPool-worker-2
// new thread - 4 : ForkJoinPool.commonPool-worker-4
// new thread - 5 : ForkJoinPool.commonPool-worker-5
// Thread : 5 : 15178
// Thread : 1 : 15178
// Thread : 4 : 15179
// Thread : 2 : 15200
// Thread : 3 : 15202
````

* doSomeRandomOperation method accesses the list and creates a new list (doesn’t modify existing list)
* Calling doSomeRandomOperation (5 times) method from Main method results in ~500 ms
* But, when the same list is accessed by 5 threads in parallel, it takes 15178 ms
