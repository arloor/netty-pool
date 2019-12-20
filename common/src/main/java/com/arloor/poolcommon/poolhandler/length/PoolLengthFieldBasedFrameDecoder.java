package com.arloor.poolcommon.poolhandler.length;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import com.arloor.poolcommon.poolhandler.PoolHandler;

public class PoolLengthFieldBasedFrameDecoder extends LengthFieldBasedFrameDecoder implements PoolHandler{

    public PoolLengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
