package com.arloor.poolclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainClient {

    private static final Logger log = LoggerFactory.getLogger(MainClient.class);

    public static void main(String[] args) {
        FixedChannelPool pool = ClientPoolBootStrap.boot();

        for (int i = 0; i < 20; i++) {
            new Thread(()->{
                while (true){
                    pool.acquire().addListener(new FutureListener<Channel>() {

                        @Override
                        public void operationComplete(Future<Channel> channelFuture) throws Exception {
                            if(channelFuture.isSuccess()){
                                //todo:获取成功，使用连接
                                channelFuture.getNow().writeAndFlush(ByteBufAllocator.DEFAULT.buffer().writeBytes("some business".getBytes()));
                                log.info("business BY "+channelFuture.getNow().id().toString());
                                //使用完毕，放回连接
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
            }).start();
        }
    }
}
