package com.ian.fix.acceptor;

import org.springframework.stereotype.Component;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.mina.acceptor.AcceptorSessionProvider;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Component
public class Acceptor {

    public Acceptor(QuickfixApplication application, SessionSettings sessionSettings)
        throws ConfigError {

        final MessageStoreFactory messageStoreFactory = new FileStoreFactory(sessionSettings);
        final LogFactory logFactory = new ScreenLogFactory();
        final MessageFactory messageFactory = new DefaultMessageFactory();

        final SocketAcceptor socketAcceptor = SocketAcceptor.newBuilder()
            .withApplication(application)
            .withSettings(sessionSettings)
            .withMessageStoreFactory(messageStoreFactory)
            .withLogFactory(logFactory)
            .withMessageFactory(messageFactory)
            .build();

        SocketAddress socketAddress = new InetSocketAddress("0.0.0.0", 9878);
        SessionID sessionID = new SessionID("FIX.4.4", "SERVER", "*");
        AcceptorSessionProvider acceptorSessionProvider = new DynamicAcceptorSessionProvider(
            sessionSettings, sessionID, application, messageStoreFactory, logFactory,
            messageFactory);
        socketAcceptor.setSessionProvider(socketAddress, acceptorSessionProvider);

        socketAcceptor.start();
    }

}
