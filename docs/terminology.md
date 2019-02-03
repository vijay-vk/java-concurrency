### Terminology

##### Concurrency vs. Parallelism
Concurrency and parallelism are related concepts, but there are small differences. Concurrency means that two or more tasks are making progress even though they might not be executing simultaneously. This can for example be realized with time slicing where parts of tasks are executed sequentially and mixed with parts of other tasks. Parallelism on the other hand arise when the execution can be truly simultaneous.  
![concurrency vs parallel](/images/parallel.png)  
From <https://medium.com/@deepshig/concurrency-vs-parallelism-4a99abe9efb8>  
From <https://doc.akka.io/docs/akka/2.5/general/terminology.html>  

##### Synchronous vs. Asynchronous
A method call is considered synchronous if the caller cannot make progress until the method returns a value or throws an exception. On the other hand, an asynchronous call allows the caller to progress after a finite number of steps, and the completion of the method may be signalled via some additional mechanism (it might be a registered callback, a Future, or a message).  
![sync vs async](/images/async.png)  
From <http://tutorials.jenkov.com/software-architecture/index.html>
From <https://doc.akka.io/docs/akka/2.5/general/terminology.html>  

##### Non-blocking vs. Blocking
We talk about blocking if the delay of one thread can indefinitely delay some of the other threads. A good example is a resource which can be used exclusively by one thread using mutual exclusion. If a thread holds on to the resource indefinitely (for example accidentally running an infinite loop) other threads waiting on the resource can not progress. In contrast, non-blocking means that no thread is able to indefinitely delay others.  
![io](/images/io.png)[io vs nio]![nio](/images/nio.png)  
From <http://tutorials.jenkov.com/java-nio/nio-vs-io.html>  
From <https://doc.akka.io/docs/akka/2.5/general/terminology.html>  

##### Process vs. Threads
A process can contain multiple threads. The biggest difference between a process and a thread, is that each process has it’s own address space, while threads (of the same process) run in a shared memory space.
![Process vs Threads](/images/process.jpg)  
From <https://i4mk.wordpress.com/2013/03/13/processes-vs-threads/>  
