package com.arloor.poolserver.businesshandler;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log=LoggerFactory.getLogger(BusinessHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String s=(String)msg;
        log.info(s);
        ctx.writeAndFlush(ByteBufAllocator.DEFAULT.buffer().writeBytes("done".getBytes()));
    }
}
