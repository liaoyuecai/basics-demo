package concurrence.thread;

import java.util.concurrent.*;

/**
 * 线程池相关
 */
public class ThreadPoolExecutorTest {
    public static void main(String[] args) {
        int corePoolSize = 10;//线程池中的核心线程数
        int maximumPoolSize = 20;//线程池中允许的最大线程数
        int keepAliveTime = 60;//线程空闲时的存活时间,默认情况下，该参数只在线程数大于corePoolSize时才有用
        TimeUnit unit = TimeUnit.SECONDS;//keepAliveTime的单位
        /**
         * 用来保存等待被执行的任务的阻塞队列，且任务必须实现Runable接口
         * 在JDK中提供了如下阻塞队列
         * 1、ArrayBlockingQueue：基于数组结构的有界阻塞队列，按FIFO排序任务
         * 2、LinkedBlockingQuene：基于链表结构的阻塞队列，按FIFO排序任务，吞吐量通常要高于ArrayBlockingQuene
         * 3、SynchronousQuene：一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQuene
         * 4、priorityBlockingQuene：具有优先级的无界阻塞队列
         * 此处为有界队列，长度为10
         */
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(10);
        /**
         * 线程池的饱和策略，当阻塞队列满了，且没有空闲的工作线程，如果继续提交任务，必须采取一种策略处理该任务，线程池提供了4种策略：
         * 1、AbortPolicy：直接抛出异常，默认策略；
         * 2、CallerRunsPolicy：用调用者所在的线程来执行任务；
         * 3、DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务；
         * 4、DiscardPolicy：直接丢弃任务；
         * 此处是自定义
         */
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("阻塞队列溢出");
            }
        };
        Executor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        final LinkedBlockingQueue queue = new LinkedBlockingQueue();
        for (int i = 0; i < 100; i++) {
            executor.execute(new Thread(){
                @Override
                public void run() {
                    System.out.println(this.getName());
                    try {
                        //利用队列人为阻塞线程，不让线程结束
                        Object o = queue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }
}

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    List<TransactionMessage> execute = new ArrayList();
    int status = 0;
    public Transaction addOperation(String queue, String operation, Object params) {
        execute.add(new TransactionMessage(queue, operation, JSON.toJSONString(params)));
        return this;
    }

    public Transaction addOperation(String queue, String operation, Object params, String remark) {
        execute.add(new TransactionMessage(queue, remark, operation, JSON.toJSONString(params)));
        return this;
    }

    public String getCurrentOperation() {
        return execute.get(0).operation;
    }

    public <T> T getParams(Class<T> clazz) {
        return JSONObject.parseObject(execute.get(0).params, clazz);
    }

    void complete(){
        execute.get(0).success();
        execute.remove(0);
        if (execute.isEmpty())
            status = 1;
    }

    void defeated(){
        execute.get(0).fail();
    }
}
import java.io.Serializable;
import java.util.UUID;

class TransactionMessage<T> implements Serializable{
    String id = UUID.randomUUID().toString();
    String queue;
    String remark;
    String operation;
    String params;
    int status = 0;

    TransactionMessage(String queue, String operation, String params) {
        this.queue = queue;
        this.operation = operation;
        this.params = params;
    }

    TransactionMessage(String queue, String remark, String operation, String params) {
        this.queue = queue;
        this.remark = remark;
        this.operation = operation;
        this.params = params;
    }

    void success(){
        status = 1;
    }

    void fail(){
        status = 2;
    }

}
