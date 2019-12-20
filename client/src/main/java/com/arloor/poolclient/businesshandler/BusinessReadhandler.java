package com.arloor.poolclient.businesshandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessReadhandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(BusinessReadhandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof String){
            log.info(msg+" BY "+ctx.channel().id());
        }
    }
}
