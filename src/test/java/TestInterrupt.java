import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuanlubin on 2017/1/12.
 */
public class TestInterrupt {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue queue = new LinkedBlockingDeque();
        Thread read = new Thread(() -> {
            while (!Thread.interrupted()) {
                System.out.println(Thread.currentThread());
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    continue;
                }
                System.out.println("{}"+Thread.interrupted());
            }
            System.out.println("break  read  thread");
        });

        Thread test = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("stop read " + read);
            read.interrupt();
        });

        read.start();
        test.start();
        read.join();
        test.join();
    }
}
