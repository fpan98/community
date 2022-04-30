package com.mininowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by FeiPan on 2022/4/30.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    public static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    /**
     * jdk线程池
     */
    // jdk线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    // jdk可定期执行任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // jdk普通线程池
    @Test
    public void testExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello executorService");
            }
        };
        for(int i=0;i<10;i++){
            executorService.submit(task);
        }
        sleep(5000);
    }

    //2、jdk定时任务线程池
    @Test
    public void testScheduledExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ScheduledExecutorService");
            }
        };
        // 会按照一定的周期不断的执行任务
        scheduledExecutorService.scheduleAtFixedRate(task, 3000, 1000, TimeUnit.MILLISECONDS);

        sleep(10000);
    }

    /**
     * spring中的线程池
     */




}
