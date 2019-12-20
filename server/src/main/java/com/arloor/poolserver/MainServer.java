package com.arloor.poolserver;

import com.arloor.poolcommon.poolhandler.length.PoolLengthFieldBasedFrameDecoder;
import com.arloor.poolcommon.poolhandler.length.PoolLengthFieldPrepender;
import com.arloor.poolserver.businesshandler.BusinessHandler;
import com.arloor.poolserver.poolhandler.ClientPingHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainServer {

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static final String POOL_SERVER_ADDR = "127.0.0.1";
    private static final int POOL_SERVER_PORT = 88;

    private static final Logger log = LoggerFactory.getLogger(MainServer.class);

    public static void main(String[] args) {
        try {
            io.netty.bootstrap.ServerBootstrap b = new io.netty.bootstrap.ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel channel) throws Exception {
                            //设定子chennel的pipeline
                            //Important: 在最前面加入pongHandler
                            channel.pipeline().addLast(new PoolLengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            channel.pipeline().addLast(new PoolLengthFieldPrepender(4));
                            channel.pipeline().addLast(ClientPingHandler.INSTANCE);


                            //todo: 增加businessHandler
                            channel.pipeline().addLast(new StringDecoder());
                            channel.pipeline().addLast(new BusinessHandler());
                        }
                    });
            b.bind(POOL_SERVER_PORT).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.warn("bind interrupted!", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
