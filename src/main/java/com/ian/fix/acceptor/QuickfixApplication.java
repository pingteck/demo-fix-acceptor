package com.ian.fix.acceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.fix44.Logon;
import quickfix.fix44.NewOrderSingle;

@Slf4j
@Component
public class QuickfixApplication implements Application {

    @Override
    public void onCreate(SessionID sessionID) {

    }

    @Override
    public void onLogon(SessionID sessionID) {

    }

    @Override
    public void onLogout(SessionID sessionID) {

    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID)
        throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        if (message instanceof Logon logon) {
            handleIncomingLogon(logon);
        }
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    }

    @Override
    public void fromApp(Message message, SessionID sessionID)
        throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (message instanceof NewOrderSingle newOrderSingle) {
            handleIncomingNewOrderSingle(newOrderSingle, sessionID);
        }
    }

    private void handleIncomingLogon(Logon message) throws RejectLogon {
        try {
            String username = message.getUsername().getValue();
            String password = message.getPassword().getValue();
            if (!("username".equals(username) && "password".equals(password))) {
                throw new RejectLogon("Invalid credentials");
            }
        } catch (FieldNotFound e) {
            throw new RejectLogon("Logon requires username and password");
        }
    }

    private void handleIncomingNewOrderSingle(NewOrderSingle message, SessionID sessionID) {
        try {
            Session.sendToTarget(message, sessionID);
        } catch (SessionNotFound e) {
            throw new RuntimeException(e);
        }
    }

}
