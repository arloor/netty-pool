package com.arloor.poolclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainClient {

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static final String POOL_SERVER_ADDR = "127.0.0.1";
    private static final int POOL_SERVER_PORT = 88;

    private static final Logger log = LoggerFactory.getLogger(MainClient.class);


    static Bootstrap b = new Bootstrap();

    static {
        b.group(workerGroup)//设定工作线程池
                .remoteAddress(POOL_SERVER_ADDR, POOL_SERVER_PORT)
                .channel(NioSocketChannel.class)//非阻塞
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
//                .handler(new ChannelInitializer<SocketChannel>() {//设定pipeline
//                    protected void initChannel(SocketChannel channel) throws Exception {
//                        //Important: 在最前面加入pingHandler
//                        channel.pipeline().addFirst(new ServerPongHandler());
//                    }
//                })
        ;
    }

    public static void main(String[] args) {
        FixedChannelPool pool = new FixedChannelPool(b, new ChannelPoolHandler() {
            @Override
            public void channelReleased(Channel channel) throws Exception {

            }

            @Override
            public void channelAcquired(Channel channel) throws Exception {

            }

            @Override
            public void channelCreated(Channel channel) throws Exception {
                channel.pipeline().addFirst(new ServerPongHandler());
            }
        }, new ChannelHealthChecker() {//根据lastPong
            @Override
            public Future<Boolean> isHealthy(Channel channel) {
                EventLoop loop = channel.eventLoop();
                return loop.newSucceededFuture(channel.pipeline().get(ServerPongHandler.class).isChannelActive());

            }
        }, FixedChannelPool.AcquireTimeoutAction.NEW, 1000, 100, 1000);


        while (true){
            pool.acquire().addListener(new FutureListener<Channel>() {

                @Override
                public void operationComplete(Future<Channel> channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        channelFuture.getNow().writeAndFlush(ByteBufAllocator.DEFAULT.buffer().writeBytes("ping".getBytes()));
                        log.info(channelFuture.getNow().id().toString());
                        pool.release(channelFuture.getNow());
                    }
                }
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


//        for (int i = 0; i < 10; i++) {
//            newConnection();
//        }
    }

    private static void newConnection() {
        //创建Bootstrap
        Bootstrap b = new Bootstrap();
        b.group(workerGroup)//设定工作线程池
                .remoteAddress(POOL_SERVER_ADDR, POOL_SERVER_PORT)
                .channel(NioSocketChannel.class)//非阻塞
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {//设定pipeline
                    protected void initChannel(SocketChannel channel) throws Exception {
                        //Important: 在最前面加入pingHandler
                        channel.pipeline().addFirst(new ServerPongHandler());
                    }
                });

        //连接操作
        b.connect().addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    log.info("connect success");
                } else {
                    log.error("connect failed!", channelFuture.cause());
                }
            }
        });
    }
}
