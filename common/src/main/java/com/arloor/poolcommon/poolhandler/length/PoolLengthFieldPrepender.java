package com.arloor.poolcommon.poolhandler.length;

import io.netty.handler.codec.LengthFieldPrepender;
import com.arloor.poolcommon.poolhandler.PoolHandler;

public class PoolLengthFieldPrepender extends LengthFieldPrepender implements PoolHandler{
    public PoolLengthFieldPrepender(int lengthFieldLength) {
        super(lengthFieldLength);
    }
}
