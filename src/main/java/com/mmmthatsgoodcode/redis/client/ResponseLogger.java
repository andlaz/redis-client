package com.mmmthatsgoodcode.redis.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ResponseLogger extends ChannelInboundHandlerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(ResponseLogger.class);
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	
		if (msg instanceof ByteBuf) {
			ByteBuf out = (ByteBuf) msg;
			LOG.debug("Inbound UTF8 decoded bytes\n{}", new String(UnpooledByteBufAllocator.DEFAULT.heapBuffer().writeBytes(out, 0, out.readableBytes()).array()));
			
		}
		
		ctx.fireChannelRead(msg);

	
	}
	
}
