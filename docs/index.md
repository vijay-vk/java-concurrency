# A Bird's-eye View on Java Concurrency Frameworks

### The why question
Few years ago when NoSQL was trending, like every other team, our team was also enthusiastic about the new and exciting stuff and we were planning to change the database in one of the Application... But, when we got into the finer details of implementation, we remembered what wise men say, _"devil is in the details"_ and eventually we realized that NoSql is not a silver bullet to fix all problems and the answer to NoSQL VS RDMS was **_"It depends"_**.  Similarly, in last one year, Concurrency libraries like RX-Java, Spring Reactor were trending with enthusiastic statements like Asynchronous Non-Blocking approach is the way to go, etc... In order to not make the same mistake again, have tried to evaluate how concurrency frameworks like ExecutorService, RX-Java, Disruptor, Akka differ from one another and how to identify the right use-case fit for respective frameworks.

#### _**Terminologies** used in this Article is described in this [link](./terminology.md)_

### Sample Use case for analyzing concurrency frameworks
![Use Case](/images/usecase.png)  

#### Quick refresher on Thread configuration
Before getting into comparison of concurrency frameworks, a quick refresher on how to configure the optimal number of threads to increase the performance of parallel tasks. This theory applies to all frameworks and the same thread configuration has been used in all frameworks to measure performance.
* For in-memory tasks, No. of threads = ~No. of cores has the best performance though it can change a bit based on hyper-threading feature in respective processor.
  * For e.g., In an 8 core machine, if each request to an Application has to do 4 in-memory tasks in parallel, then the load on this machine should be maintained @ 2 req/sec with 8 threads in ThreadPool.
* For I/O tasks, number of threads configured in ExecutorService should be based on latency of external service  
  * For e.g., In an 8 core machine, if each request to an Application has to do 4 I/O tasks in parallel, then the load of this machine can be roughly around 4 req/sec with 16 threads configured in ThreadPool
  * Unlike in-memory task, the thread involved in I/O task will be blocked and it will be in waiting state until external service responds or times out. So, when I/O tasks are involved, as the threads are blocked, the number of threads should be increased to handle the additional load from concurrent requests.
  * Number of threads for I/O task should be increased in a conservative way as many threads in Active state brings the cost of context-switching which will impact the Application performance. To avoid that, the exact number of threads and load of this machine should be increased proportionately to the waiting time of the threads involved in I/O task.  

Reference : <http://baddotrobot.com/blog/2013/06/01/optimum-number-of-threads/>

#### Performance Results
*Performance tests ran in GCP -> processor model name: Intel(R) Xeon(R) CPU @ 2.30GHz; Architecture: x86_64; No. of cores : 8   (Note : These results are subjective to this use-case and doesn't imply one framework is better than other)*

Label | # of requests | Thread Pool size for I/O Tasks | Average Latency in ms (50 req/sec)
All the operations are in Sequential order | ~10000 | NA | ~2100
Parallelize IO Tasks with Executor Service and use http-thread for in-memory task | ~10000 | 16 | ~1800
Parallelize IO Tasks with Executor Service (Completable Future) and use http-thread for in-memory task | ~10000 | 16 | ~1800
Parallelize All tasks with ExecutorService and use `@Suspended AsyncResponse response` to send response in non-blocking manner  | ~10000 | 16 | ~3500
Use Rx-Java for performing all tasks and use `@Suspended AsyncResponse response` to send response in non-blocking manner  | ~10000 | 16 | ~2300
Parallelize All tasks with Disruptor framework (Http thread will be blocked) | ~10000 | 11 | ~3000
Parallelize All tasks with Disruptor framework and use `@Suspended AsyncResponse response` to send response in non-blocking manner | ~10000 | 12 | ~3500
Parallelize All tasks with Akka framework (Http thread will be blocked) | ~10000 | | ~3000

### Parallelize IO Tasks with Executor Service  

###### When to use?  
If an Application is deployed in multiple nodes and if req/sec in each node is less than the no. of cores available, then Executor Service can be used to parallelize tasks and execute faster.

###### When not to use?  
If an Application is deployed in multiple nodes and if req/sec in each node is much higher than the no. of cores available, then using ExecutorService to further parallelize can only make things worse.

#### Performance results when delay of external service is increased to 400 ms (request rate @ 50 req/sec in 8 core machine)

  Label | # of requests | Thread Pool size for I/O Tasks | Average Latency in ms (50 req/sec)
  All the operations are in Sequential order | ~3000 | NA | ~2600
  Parallelize IO Tasks with Executor Service and use http-thread for in-memory task | ~3000 | 24 | ~3000

##### Code Example when all tasks are executed in sequential order.
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
##### Code Example when I/O tasks are executed in parallel with ExecutorService
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

### Parallelize IO Tasks with Executor Service (CompletableFuture)
* This works similar to the above case; Http thread which handles the incoming request will be blocked and CompletableFuture is used to handle the parallel tasks

###### When to use?
Without AsyncResponse, performance is same as ExecutorService; If multiple API calls has to be async and if it has to be chained, this approach is better. (this is similar to Promises in Node)

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
![io](/images/io.png)[io vs nio]![nio](/images/nio.png)  
From <http://tutorials.jenkov.com/java-nio/nio-vs-io.html>  
* Http thread which handles the Connection of incoming request will pass the processing to Executor Pool and when all tasks are done, another http-thread will send the response back to client. (Asynchronous and non-blocking)
* Reason for drop in performance
  * In synchronous communication, **though the thread involved in I/O task was blocked, the process will still be in running state as long as it has additional threads to take the load of concurrent requests.**
  * So, the benefit which comes from a keeping a thread in non-blocking manner is very less and the cost involved to handle the request in this pattern seems to be high.
  * More often than not, **using asynchronous non-blocking approach for use case like we discuss here, will bring down Application performance.**

###### When to use?
If use case is like a server-side chat Application where a thread need not hold the Connection until client responds, then Async non-blocking approach can be preferred over synchronous communication; in those use cases, rather than just waiting, system resources can be put to better use with asynchronous non-blocking approach.

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

### Rx-Java
* This is similar to above case; only difference is RX-Java gives better DSL to write code in fluid manner (may not be visible with this example)
* Performance is better than handling parallel tasks with Completable Future

###### When to use?
If Asynchronous non-blocking approach suits a use-case, then RX-Java or any reactive libraries can be preferred (It has additional capabilities like back-pressure which can balance the load between producers and consumers)  

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

### Disruptor
![io](/images/queue.png)  
**[Queue vs RingBuffer]**
![io](/images/disruptor.jpg)  
From <http://tutorials.jenkov.com/java-concurrency/blocking-queues.html>  
From <https://www.baeldung.com/lmax-disruptor-concurrency>  
* In this example, http-thread will be blocked until disruptor completes the tasks and a CountDownLatch has been used to synchronize the http-thread with the threads from ExecutorService.
* Main feature of this framework is to handle inter-thread communication without any locks; In ExecutorService, the data between a producer and consumer will be passed via a Queue and there is a **Lock** involved during this data transfer between a producer and a consumer. Disruptor framework handles this Producer-Consumer communication without any Locks with the help of a data-structure called as Ring Buffer which is an extended version of a Circular Array Queue.
* _This library was not meant for use cases like the one we discuss here; It has been added just out of curiosity..._

###### When to use?
It performs better when used with event-driven architectural patterns and when there is a single producer and multiple consumers with main focus on in-memory tasks.

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

### Akka
![Akka-Actors](/images/actor.png)  
From <https://blog.codecentric.de/en/2015/08/introduction-to-akka-actors/>  
* The main advantage of Akka library is, it has native support to build Distributed Systems.
* It runs on a system called as Actor System which abstracts the concept of Threads and Actors in the Actor System communicate via asynchronous messages which is similar to the communication between a Producer and Consumer.
* This extra level of Abstraction helps the Actor system to provide features like [Fault Tolerance](https://doc.akka.io/docs/akka/2.5/fault-tolerance.html), [Location Transparency](https://doc.akka.io/docs/akka/2.5/general/remoting.html), ...
* With the right Actor to Thread strategy, this framework can be optimized to perform better than the results shown in above table. Though, it cannot match the performance of a traditional approach on a single node, it can still be preferred for it's capabilities to build Distributed and Resilient systems.

###### Sample code

```java

// from controller :
Actors.masterActor.tell(new Master.Request("Get Response", event, Actors.workerActor), ActorRef.noSender());

// handler :
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

#### A special case (when shared memory between threads crosses ~8MB) has been discussed in this [link](./java-cache-miss.md)

### Summary :
* Decide Executor framework's configuration based on the load of the machine and also check if load balancing can be done based on the number of parallel tasks in the Application.
* Using Reactive or any Asynchronous libraries decreases the performance for most of the traditional applications. This pattern is useful only when the use case is like a server-side chat application, where the thread need not retain the connection until the client responds.
* Performance of Disruptor framework was good when used with event-driven architectural patterns; but when disruptor pattern was mixed with a traditional architecture and for a use case as we discussed here, it was not up-to the mark. (Akka and Disruptor libraries deserve a separate post by using with event-driven architectural patterns)
