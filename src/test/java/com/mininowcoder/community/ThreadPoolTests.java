package com.mininowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
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
    /*
    // spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // spring定时执行线程池
    // 需要写一个配置类，并添加@EnableScheduling, @EnableAsync
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Test
    public void testThreadPoolTaskExecutor(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ThreadPoolTaskExecutor");
            }
        };
        for(int i=0;i<10;i++){
            taskExecutor.submit(task);
        }
        sleep(5000);
    }

    @Test
    public void testThreadPoolTaskScheduler(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ThreadPoolTaskScheduler");
            }
        };
        Date start = new Date(System.currentTimeMillis()+3000);
        taskScheduler.scheduleAtFixedRate(task, start, 1000);//默认单位是毫秒
        sleep(10000);
    }

    // @Async 让该方法在多线程环境下，被异步的调用
    @Async
    public void execute1(){
        logger.debug("execute1");
    }
    @Test
    public void testAsync(){
        for(int i=0;i<10;i++)
            execute1();// spring内部会使用线程池调用执行该方法
        sleep(10000);
    }

    // 注解，定时执行任务
    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void execute2(){
        logger.debug("execute2");
    }
    @Test
    public void testScheduled(){
        sleep(30000); // 只要有程序在跑，加了@Scheduled的定时任务就会自动执行
    }
     */


}
