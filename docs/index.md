# A bird's-eye view on Java concurrency frameworks

### Objective
  * Compare different concurrency frameworks to identify the right use-case fit for respective frameworks. (with focus on inter-thread communication features)
  * How to identify the number of threads required for an application ?

### Sample Use case for analyzing different concurrency frameworks

![Use Case](/images/usecase.png)

* Http Request made to Application
* Application has to make few network calls and do some CPU Intensive operation before sending response
* In this use case, 4 network calls will be made for each request
* When combining API response – CPU Intensive operation (in-memory tasks) has been mocked in following way:
  * When combining API Response from /posts and /comments API -> Get the pre-defined list (static) which has a list of Integers; multiply each number with a random number to create a new list
    * Case 1: When shared memory between threads is around ~5MB (Integer list size - 1250000)
    * Case 2: When shared memory between threads is around ~10MB (Integer list size - 2500000)
  * When combining API response from /albums and /photos API -> Get the pre-defined list (static) which has list of Long values; multiply each number with a random number to create a new list
    * Case 1: When shared memory between threads is around ~5MB (Long list size - 625000)
    * Case 2: When shared memory between threads is around ~10MB (Long list size - 1250000)
* Combine all response and send the response back to client

### Case 1 (Shared memory between threads less than ~8 MB - Analysis with few concurrency frameworks)
#### Performance Results
*These results are subjective to this use-case and doesn't imply one framework is better than other*

Label | # of requests | Thread Pool size for I/O Tasks | Average Latency in ms (50 req/sec)
All the operations are in Sequential order | ~10000 | NA | ~2100
Parallelize IO Tasks with Executor Service and use http-thread for in-memory task | ~10000 | 16 | ~1800
Parallelize IO Tasks with Executor Service (Completable Future) and use http-thread for in-memory task | ~10000 | 16 | ~1800
Parallelize All tasks with ExecutorService and use `@Suspended AsyncResponse response` to send response in non-blocking manner  | ~10000 | 16 | 3500
Use Rx-Java for performing all tasks and use `@Suspended AsyncResponse response` to send response in non-blocking manner  | ~10000 | 16 | 2300
Parallelize All tasks with Disruptor framework (Http thread will be blocked) | ~10000 | 11 | ~3000
Parallelize All tasks with Disruptor framework and use `@Suspended AsyncResponse response` to send response in non-blocking manner | ~10000 | 12 | 3500
Parallelize All tasks with Akka framework (Http thread will be blocked) | ~10000 | | ~3500

#### Parallelize IO Tasks with Executor Service
* Default way to parallelize tasks in Java
* No. Of Threads created to run parallel tasks had an impact on Performance
  * For CPU Intensive tasks (in-memory tasks) - No. of threads = No. of cores + 1 works well
  * For I/O tasks (API/ Database calls) - No. of threads = No. of cores * 2 (can be the default formula)
  * If the idle time (waiting time) in I/O operation increases; then no. of threads to handle I/O tasks can be increased.
  * For e.g., if each API call took ~100ms and Executor pool size was 8; when the consuming API's average latency increases to ~200ms, increasing Executor pool size to 16 performs better. (Reason : Threads are blocked until it receives response or until timeout - additional threads helps in this case to handle the extra load)
  * But, if latency is even higher, **sequential execution of tasks performs better than handling the tasks in parallel** with 20+ threads (**More threads brings the cost of context-switching**) With single request, parallelizing tasks with executors can perform better; but with concurrent requests, sequential execution performs better. For e.g., when the consuming API's delay was increased to 400 ms from 200 ms, performance of sequential execution looks better.
  * This result is subjective; it's always good to have a toggle between sequential and parallel execution (decision can be made with metrics)

##### Performance results when API delay is increased to 400 ms

  Label | # of requests | Thread Pool size for I/O Tasks | Average Latency in ms (50 req/sec)
  All the operations are in Sequential order | ~3000 | NA | ~2600
  Parallelize IO Tasks with Executor Service and use http-thread for in-memory task | ~3000 | 24 | ~3000

##### Code
###### Sequential
```java
long startTimeOfIOTasks = System.currentTimeMillis();
String posts = JsonService.getPosts();
String comments = JsonService.getComments();
String albums = JsonService.getAlbums();
String photos = JsonService.getPhotos();
long endTimeOfIOTasks = System.currentTimeMillis();
long timeTakenOfIOTasks = endTimeOfIOTasks - startTimeOfIOTasks;
LOG.info("Time Taken for Sequential Service IO Operations :: " + timeTakenOfIOTasks + " - in Thread "
		+ Thread.currentThread().getName());

long startTimeOfNonIOTasks = System.currentTimeMillis();

int userId = new Random().nextInt(10) + 1;
String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments);
String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos);

String response = postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;

long endTimeOfNonIOTasks = System.currentTimeMillis();
long timeTakenOfNonIOTasks = endTimeOfNonIOTasks - startTimeOfNonIOTasks;
long timeTaken = endTimeOfNonIOTasks - startTimeOfIOTasks;
LOG.info("Time Taken for Sequential Service non-IO Operations :: " + timeTakenOfNonIOTasks + " - in Thread "
		+ Thread.currentThread().getName());
LOG.info("Time Taken for Sequential Service to build response :: " + timeTaken + " - in Thread "
		+ Thread.currentThread().getName());

return response;
```
###### Code : ExecutorService
```java
List<Callable<String>> ioCallableTasks = new ArrayList<>();
ioCallableTasks.add(JsonService::getPosts);
ioCallableTasks.add(JsonService::getComments);
ioCallableTasks.add(JsonService::getAlbums);
ioCallableTasks.add(JsonService::getPhotos);

ExecutorService ioExecutorService = CustomThreads.getExecutorService(ioPoolSize);
List<Future<String>> futuresOfIOTasks = ioExecutorService.invokeAll(ioCallableTasks);

String posts = futuresOfIOTasks.get(0).get();
String comments = futuresOfIOTasks.get(1).get();
String albums = futuresOfIOTasks.get(2).get();
String photos = futuresOfIOTasks.get(3).get();

String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments);
String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos);

return postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
```

#### Parallelize IO Tasks with Executor Service (CompletableFuture)
* This works similar to the above one; Http thread which handles the incoming request will be blocked and CompletableFuture is used to handle the parallel tasks
* Only difference is, CompletableFuture provides DSL to pass call back function and chain sequence of events. (similar to Promise in node application; performance was similar to the previous one)

```java
int userId = new Random().nextInt(10) + 1;
ExecutorService ioExecutorService = CustomThreads.getExecutorService(ioPoolSize);
CompletableFuture<String> postsFuture = CompletableFuture.supplyAsync(JsonService::getPosts, ioExecutorService);
CompletableFuture<String> commentsFuture = CompletableFuture.supplyAsync(JsonService::getComments,
    ioExecutorService);
CompletableFuture<String> albumsFuture = CompletableFuture.supplyAsync(JsonService::getAlbums,
    ioExecutorService);
CompletableFuture<String> photosFuture = CompletableFuture.supplyAsync(JsonService::getPhotos,
    ioExecutorService);

CompletableFuture.allOf(postsFuture, commentsFuture, albumsFuture, photosFuture).get();

String posts = postsFuture.get();
String comments = commentsFuture.get();
String albums = albumsFuture.get();
String photos = photosFuture.get();

String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments);
String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos);

return postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
```

#### Parallelize All tasks with ExecutorService and use `@Suspended AsyncResponse response` to send response in non-blocking way
* Http thread which handles the Connection of incoming request will pass the processing to Executor Pool and when all tasks are done, another http-thread will send the response back to client. (Asynchronous and non-blocking)
* Reason for drop in performance
  * non-blocking is an overloaded terminology; **In synchronous communication - yes, thread was blocked; but the process is not blocked - cpu-cores can not blocked when thread was waiting for I/O task;** cpu-cores can run instructions from other threads; (so, creating additional threads helps to handle additional load with the cost of thread context-switching)
  * Cost of handling asynchronous non-blocking communication was little high and it's not as optimized as the traditional synchronous http libraries.
  * Also, looks like, this approach was not meant for use-case like this, asynchronous non-blocking approach is meant for use cases like server-side chat application. (Where a thread need not hold the connection until the client responds back; client may reply back in ~5 min, ~10 mins, ...)
  * **More often than not, using asynchronous non-blocking approach for use case like we discuss here, will bring down Application performance.**

```java
int userId = new Random().nextInt(10) + 1;
ExecutorService ioExecutorService = CustomThreads.getExecutorService(ioPoolSize);
CompletableFuture<String> postsFuture = CompletableFuture.supplyAsync(JsonService::getPosts, ioExecutorService);
CompletableFuture<String> commentsFuture = CompletableFuture.supplyAsync(JsonService::getComments,
		ioExecutorService);
CompletableFuture<String> albumsFuture = CompletableFuture.supplyAsync(JsonService::getAlbums,
		ioExecutorService);
CompletableFuture<String> photosFuture = CompletableFuture.supplyAsync(JsonService::getPhotos,
		ioExecutorService);

CompletableFuture<String> postsAndCommentsFuture = postsFuture.thenCombineAsync(commentsFuture,
		(posts, comments) -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments),
		ioExecutorService);

CompletableFuture<String> albumsAndPhotosFuture = albumsFuture.thenCombineAsync(photosFuture,
		(albums, photos) -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos),
		ioExecutorService);

postsAndCommentsFuture.thenAcceptBothAsync(albumsAndPhotosFuture, (s1, s2) -> {
	LOG.info("Building Async Response in Thread " + Thread.currentThread().getName());
	String response = s1 + s2;
	asyncHttpResponse.resume(response);
}, ioExecutorService);
```

#### Rx-Java
* This is similar to above case; only difference is RX-Java gives better DSL to write code in fluid manner (may not be visible with this example)
* Performance is better than handling parallel tasks with Completable Future
* RX-Java seems to be better for use-cases where a back-pressure was required to handle the no. of events between producers and consumers (if Asynchronous non-blocking threads suits a use-case, then RX-Java or any reactive libraries can be preferred)

```java
int userId = new Random().nextInt(10) + 1;
ExecutorService executor = CustomThreads.getExecutorService(8);

Observable<String> postsObservable = Observable.just(userId).map(o -> JsonService.getPosts())
		.subscribeOn(Schedulers.from(executor));
Observable<String> commentsObservable = Observable.just(userId).map(o -> JsonService.getComments())
		.subscribeOn(Schedulers.from(executor));
Observable<String> albumsObservable = Observable.just(userId).map(o -> JsonService.getAlbums())
		.subscribeOn(Schedulers.from(executor));
Observable<String> photosObservable = Observable.just(userId).map(o -> JsonService.getPhotos())
		.subscribeOn(Schedulers.from(executor));

Observable<String> postsAndCommentsObservable = Observable
		.zip(postsObservable, commentsObservable,
				(posts, comments) -> ResponseUtil.getPostsAndCommentsOfRandomUser(userId, posts, comments))
		.subscribeOn(Schedulers.from(executor));

Observable<String> albumsAndPhotosObservable = Observable
		.zip(albumsObservable, photosObservable,
				(albums, photos) -> ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, albums, photos))
		.subscribeOn(Schedulers.from(executor));

Observable.zip(postsAndCommentsObservable, albumsAndPhotosObservable, (r1, r2) -> r1 + r2)
		.subscribeOn(Schedulers.from(executor))
		.subscribe((response) -> asyncResponse.resume(response), e -> asyncResponse.resume("error"));
```

#### Disruptor
* In this first example of Disruptor, http-thread will be blocked until disruptor completes the tasks.
* This library was not meant for use cases like the one we discuss here. (Added this, just out of curiosity)
* It performs better when used with event driven architectural patterns and where there is a single producer and multiple consumers. (It handles inter-thread communication without any locks (unlike ExecutorService/Akka) and there is significant performance difference in those use-cases)

```java
static {
int userId = new Random().nextInt(10) + 1;
// Sample Event-Handler; count down latch is used to synchronize the thread with http-thread
  EventHandler<Event> postsApiHandler = (event, sequence, endOfBatch) -> {
  	event.posts = JsonService.getPosts();
  	event.countDownLatch.countDown();
  };

  DISRUPTOR.handleEventsWith(postsApiHandler, commentsApiHandler, albumsApiHandler)
  		.handleEventsWithWorkerPool(photosApiHandler1, photosApiHandler2)
  		.thenHandleEventsWithWorkerPool(postsAndCommentsResponseHandler1, postsAndCommentsResponseHandler2)
  		.handleEventsWithWorkerPool(albumsAndPhotosResponseHandler1, albumsAndPhotosResponseHandler2);
  DISRUPTOR.start();
}

// for each request :
Event event = null;
RingBuffer<Event> ringBuffer = DISRUPTOR.getRingBuffer();
long sequence = ringBuffer.next();
CountDownLatch countDownLatch = new CountDownLatch(6);

try {
	event = ringBuffer.get(sequence);
	event.countDownLatch = countDownLatch;
	event.startTime = System.currentTimeMillis();
} finally {
	ringBuffer.publish(sequence);
}

try {
	event.countDownLatch.await();
} catch (InterruptedException e) {
	e.printStackTrace();
}
```

#### Akka
* akka-actors is used for handling parallel tasks.
* It can be optimized to perform better than the results shown in above table (but it can't match the traditional approach)
* Akka libraries are a better fit for use cases where, supervision and monitoring capabilities of child tasks are important and where inter-thread communication has to be scaled-out with inter-process communication (and there are many more features from this library...)
###### Sample code
```java
public Receive createReceive() {
  return receiveBuilder().match(Request.class, request -> {

  Event event = request.event; // Ideally, immutable data structures should be used here.
  request.worker.tell(new JsonServiceWorker.Request("posts", event), getSelf());
  request.worker.tell(new JsonServiceWorker.Request("comments", event), getSelf());
  request.worker.tell(new JsonServiceWorker.Request("albums", event), getSelf());
  request.worker.tell(new JsonServiceWorker.Request("photos", event), getSelf());
  }).match(Event.class, e -> {
  if (e.posts != null && e.comments != null & e.albums != null & e.photos != null) {
  	int userId = new Random().nextInt(10) + 1;
  	String postsAndCommentsOfRandomUser = ResponseUtil.getPostsAndCommentsOfRandomUser(userId, e.posts,
  			e.comments);
  	String albumsAndPhotosOfRandomUser = ResponseUtil.getAlbumsAndPhotosOfRandomUser(userId, e.albums,
  			e.photos);

  	String response = postsAndCommentsOfRandomUser + albumsAndPhotosOfRandomUser;
  	e.response = response;
  	e.countDownLatch.countDown();
  }
  }).build();
}
```

### Case 2 (when shared memory between threads is greater than ~8 MB)
* This can happen for use cases like rule engine - where all incoming requests are validated against pre-defined rules and these rules can grow over time crossing the 8 MB limit.
* When the shared memory between threads crosses ~8 MB, then handling in-memory tasks with single thread had much better Performance
* The size (~8 MB) will change based on the processor
* _Performance was better with single thread execution, as handling huge in-memory data with multiple threads leads to **Cache-Miss** during execution_

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


### Summary :
* Decide concurrency frameworks based on the use-case (Also, in some cases, sequential execution may perform better; always have a toggle between sequential execution and parallel execution; measure and decide which performs better)
* Using Reactive (or any Asynchronous libraries) decreases the performance for most of the traditional applications (It's useful only when the use case is like server-side chat application, where the thread need not retain the connection until the client responds)
* Performance of Disruptor framework was good when used with event-driven architectural patterns; but when used in a use case as discussed here (with the traditional architecture), it was not upto the mark. (Akka and Disruptor libraries deserve a separate post by using with event driven patterns)
