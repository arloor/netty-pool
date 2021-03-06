package com.arloor.poolclient;

import com.arloor.poolclient.businesshandler.BusinessReadhandler;
import com.arloor.poolclient.poolhandler.ServerPongHandler;
import com.arloor.poolcommon.poolhandler.length.PoolLengthFieldBasedFrameDecoder;
import com.arloor.poolcommon.poolhandler.length.PoolLengthFieldPrepender;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class ClientPoolBootStrap {
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static final String POOL_SERVER_ADDR = "api.arloor.com";
    private static final int POOL_SERVER_PORT = 88;

    private static final Logger log = LoggerFactory.getLogger(MainClient.class);


    //用于连接池的Bootstrap
    static Bootstrap b = new Bootstrap();
    static {
        b.group(workerGroup)//设定工作线程池
                .remoteAddress(POOL_SERVER_ADDR, POOL_SERVER_PORT)
                .channel(NioSocketChannel.class)//非阻塞
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
        //当用于连接池时，下面的设定pipeline不起作用，需要用ChannelPoolHandler的ChannelCreated方法
//                .poolhandler(new ChannelInitializer<SocketChannel>() {//设定pipeline
//                    protected void initChannel(SocketChannel channel) throws Exception {
//                        //Important: 在最前面加入pingHandler
//                        channel.pipeline().addFirst(new ServerPongHandler());
//                    }
//                })
        ;
    }

    public static FixedChannelPool boot() {
        FixedChannelPool pool = new FixedChannelPool(b, new ChannelPoolHandler() {
            @Override
            public void channelReleased(Channel channel) throws Exception {
                //Important： 事实证明：在acquire和release中增减handler不行，所以以下代码注释掉
                //删除所有非PoolHandler实现类的handler
//                channel.pipeline().forEach(cell->{
//                    if(!(cell.getValue() instanceof PoolHandler)){
//                        channel.pipeline().remove(cell.getValue());
//                    }
//                });

            }

            @Override
            public void channelAcquired(Channel channel) throws Exception {

            }

            @Override
            public void channelCreated(Channel channel) throws Exception {
                //增加poolhandler
                channel.pipeline().addLast(new PoolLengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                channel.pipeline().addLast(new PoolLengthFieldPrepender(4));
                channel.pipeline().addLast(new ServerPongHandler());
                //todo: 增加businessHandler
                Class init=Class.forName("com.arloor.poolclient.BusinessHandlerInit");
                Method method=init.getMethod("addHandler",Channel.class);
                method.invoke(init,channel);
            }
        }, new ChannelHealthChecker() {//根据lastPong
            @Override
            public Future<Boolean> isHealthy(Channel channel) {
                EventLoop loop = channel.eventLoop();
                return loop.newSucceededFuture(channel.isActive()&&channel.pipeline().get(ServerPongHandler.class).hasHeartbeat());

            }
        }, FixedChannelPool.AcquireTimeoutAction.NEW, 1000, 100, 1000);


        return pool;
    }
}
