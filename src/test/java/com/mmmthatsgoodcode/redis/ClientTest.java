package com.mmmthatsgoodcode.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.mmmthatsgoodcode.redis.client.monitor.LoggingMonitor;
import com.mmmthatsgoodcode.redis.protocol.PendingResponse;
import com.mmmthatsgoodcode.redis.protocol.Response;
import com.mmmthatsgoodcode.redis.protocol.request.Get;
import com.mmmthatsgoodcode.redis.protocol.request.Ping;
import com.mmmthatsgoodcode.redis.protocol.request.Set;

public class ClientTest {
	
	private static Client CLIENT;
	
	@BeforeClass
	public static void createClient() {
		
		CLIENT = new Client.Builder()
		.addHost("127.0.0.1", 6379)
		.addHost("127.0.0.1", 6380)
		.addMonitor(new LoggingMonitor())
		.withSendWaitStrategy(new SleepingWaitStrategy())
		.shouldBatch(false)
		.shouldHash(true)
		.build();
		
		CLIENT.connect();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	@Test
	@Ignore
	public void testSimpleCommands() throws InterruptedException {
		
		CLIENT.send(new Ping());
		
		Thread.sleep(1000);
		
		
	}
	
	@Test
	public void multiThreadedPipelineTest() throws InterruptedException {
				
		ExecutorService executor = Executors.newFixedThreadPool(8);
		
		final BlockingQueue<PendingResponse> responses = new LinkedBlockingQueue<PendingResponse>();
		
		for (int r=1; r <= 10; r++) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					String id = UUID.randomUUID().toString();
					responses.add( CLIENT.send(new Set(id, "i'm really really random")) );
					responses.add( CLIENT.send(new Get(id)) );

				}
				
			});
		}
		
		executor.shutdown();
		executor.awaitTermination(60, TimeUnit.SECONDS);
				
		for(PendingResponse response:responses) {
			try {
				System.out.println(response.get(1, TimeUnit.SECONDS).value());
			} catch (ExecutionException | TimeoutException e) {
				System.err.println("Timed out");
			}
		}
		
		
	}
	
	
	@Test
	@Ignore
	public void singleThreadedPipelineTest() throws InterruptedException {
		
		final List<PendingResponse> responses = new ArrayList<PendingResponse>();

		for (int r=1; r <= 5; r++) {

			String id = UUID.randomUUID().toString();
			responses.add( CLIENT.send(new Set(id, "i'm really really random")) );
			responses.add( CLIENT.send(new Get(id)) );

		}		
		
		
		for(PendingResponse response:responses) {
			try {
				System.out.println(response.get(2, TimeUnit.SECONDS));
			} catch (ExecutionException | TimeoutException e) {
				System.err.println("Timed out");
			}
		}
				
	}
	
}
