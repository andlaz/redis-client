package com.mmmthatsgoodcode.redis.disruptor.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.mmmthatsgoodcode.redis.Client;
import com.mmmthatsgoodcode.redis.Host;
import com.mmmthatsgoodcode.redis.protocol.Pipeline;

/**
 * Routes requests to a RedisHost
 * @author andras
 *
 */
public class RequestRouter implements EventHandler<RequestEvent> {

	private final static Logger LOG = LoggerFactory.getLogger(RequestRouter.class);
	private final Client client;
	
	public RequestRouter(Client client) {
		this.client = client;
	}
	
	@Override
	public void onEvent(RequestEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		
			if (event.getHash() != null) client.getHosts().get(Hashing.consistentHash(event.getHash(), client.getHosts().size())).send(event.getRequest());
			else client.getHosts().get(new Random().nextInt(client.getHosts().size())).send(event.getRequest());

		
	}


}
