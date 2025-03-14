package com.ian.fix.acceptor;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MarketDataRequestException extends RuntimeException {

    private final char mdReqRejReason;

    public MarketDataRequestException(char mdReqRejReason, String message) {
        super(message);
        this.mdReqRejReason = mdReqRejReason;
    }

}
