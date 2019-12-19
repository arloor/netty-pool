package com.arloor.poolclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * 处理服务端的PONG心跳
 * 要放在pipeline的最开始！！
 */
public class ServerPongHandler extends ChannelInboundHandlerAdapter {

    private static final byte[] PONG = "pong".getBytes();
    private static final int PING_INTERVAL = 100000;//单位s
    private static final int fazhi = 2*PING_INTERVAL*1000; //n倍于INTERVAL

    private static final Logger log = LoggerFactory.getLogger(ServerPongHandler.class);

    private long lastPing = -1;
    private long lastPong = -1;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel active AND start PING");
        //设置定时任务ping,以后会每隔一段时间就发送PING，直到某个PING发送失败
        setDelayPing(ctx);
        //设置最后一次响应时间为该CHannel的active时间，没问题。
        lastPong=System.currentTimeMillis();
        super.channelActive(ctx);
    }


    /**
     * 仅仅处理PING
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            if (buf.readableBytes() == PONG.length) {
                buf.markReaderIndex();
                //检测：如果非PING，则继续往下走
                for (int i = 0; i < PONG.length; i++) {
                    if (buf.readByte() != PONG[i]) {
                        buf.resetReaderIndex();
                        ctx.fireChannelRead(msg);
                        return;
                    }
                }
                //走到这里就说明是PING
                lastPong=System.currentTimeMillis();
                log.info("receive PONG");
                //从ctx发送PONG，确保不走后面的ChannelOutBounder

            } else {
                ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }


    /**
     * 设置延时任务PING
     * 在延时后，发送PING
     * @param ctx
     */

    private void setDelayPing(ChannelHandlerContext ctx){
        ctx.executor().schedule(() -> {
            sendPing(ctx);
        }, PING_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 发送PING
     * 如果PING成功，则设置下一次延迟PING
     * @param ctx
     */
    private void sendPing(ChannelHandlerContext ctx) {
        ByteBuf PING = ByteBufAllocator.DEFAULT.buffer().writeBytes("ping".getBytes());
        ctx.writeAndFlush(PING).addListener(future -> {
            if (future.isSuccess()) {
                 lastPing=System.currentTimeMillis();
                log.info("PING success");
                setDelayPing(ctx);
            } else {
                log.error("PING failed!", future.cause());
            }
        });
    }

    /**
     * 根据lastPong判断是否存活
     * @return
     */
    public boolean isChannelActive(){
        if(System.currentTimeMillis() - lastPong > fazhi){
            return false;
        }else {
            return true;
        }
    }


}
