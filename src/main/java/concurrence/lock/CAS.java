package concurrence.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CAS（Compare and swap）
 * 比较和替换是使用一个期望值和一个变量的当前值进行比较，如果当前变量的值与我们期望的值相等，就使用一个新值替换当前变量的值。
 * CAS属于乐观锁，基于硬件实现，不需要进入内核，适合竞争较少的资源
 * 如果竞争较大的资源，CAS自旋概率会增加，性能反而下降
 */
public class CAS {
    static class TakeNo {
        AtomicInteger locked;
        int number;

        TakeNo(int number) {
            this.number = number;
            locked = new AtomicInteger(number);
        }

        int takeNumberByCAS() {
            return locked.getAndDecrement();
        }

        synchronized int takeNumberBySynchronized() {
            return number--;
        }
    }

    static class Count {
        long start = 0;
        long end = 0;

        public void setStart(long start) {
            this.start = start;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        long getInterval() {
            return end - start;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        /**
         * 设计场景：100人抢号，不能重复
         * 测试CAS（乐观锁）与synchronized（悲观锁/独占锁）在资源竞争严重的情况下性能对比
         * 测试结果：在资源竞争严重的情况下，独占锁性能优于CAS
         */
        final TakeNo s = new TakeNo(10000);
        final Count count1 = new Count();
        final Count count2 = new Count();
        ExecutorService executorService = Executors.newCachedThreadPool();
        long start = 0;
        long end = 0;
        for (int i = 0; i < 10000; i++) {
            executorService.execute(new Thread() {
                public void run() {
                    int a = s.takeNumberByCAS();
                    if (a == 10000) {
                        count1.setStart(System.currentTimeMillis());
                    }
                    if (a == 1) {
                        count1.setEnd(System.currentTimeMillis());
                        System.out.println("CAS time:" + count1.getInterval());
                    }
                }
            });
        }

        for (int i = 0; i < 10000; i++) {
            executorService.execute(new Thread() {
                public void run() {
                    int a = s.takeNumberBySynchronized();
                    if (a == 10000) {
                        count2.setStart(System.currentTimeMillis());
                    }
                    if (a == 1) {
                        count2.setEnd(System.currentTimeMillis());
                        System.out.println("Synchronized time:" + count2.getInterval());
                    }
                }
            });
        }
    }
}
