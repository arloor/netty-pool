package com.arloor.poolserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 处理客户端的PING心跳
 * 要放在pipeline的最开始！！
 */
@ChannelHandler.Sharable//因为服务器端不需要保存心跳状态，所以可以share，并且使用单例
public class ClientPingHandler extends ChannelInboundHandlerAdapter {

    private static final byte[] PING = "ping".getBytes();

    private static final Logger log = LoggerFactory.getLogger(ClientPingHandler.class);

    public static final ClientPingHandler INSTANCE =new ClientPingHandler();

    private ClientPingHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            if (buf.readableBytes() == PING.length) {
                buf.markReaderIndex();
                //检测：如果非PING，则继续往下走
                for (int i = 0; i < PING.length; i++) {
                    if (buf.readByte() != PING[i]) {
                        buf.resetReaderIndex();
                        ctx.fireChannelRead(msg);
                        return;
                    }
                }
                //走到这里就说明是PING
                log.info("receive PING");
                //从ctx发送PONG，确保不走后面的ChannelOutBounder
                final ByteBuf PONG = ByteBufAllocator.DEFAULT.buffer().writeBytes("pong".getBytes());
                ctx.writeAndFlush(PONG).addListener(future -> {
                    if (future.isSuccess()){
                        log.info("PONG success");
                    }else{
                        log.error("PONG failed!",future.cause());
                    }
                });
            } else {
                ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
