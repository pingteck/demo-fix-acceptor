package com.ian.fix.acceptor;

import java.util.UUID;
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
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;
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
        } else {
            throw new UnsupportedMessageType();
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

    private void handleIncomingNewOrderSingle(NewOrderSingle message, SessionID sessionID)
        throws FieldNotFound {
        try {
            final ExecutionReport pendingNew = createPendingNewExecutionReport(message);
            sendMessage(pendingNew, sessionID);
        } catch (IllegalArgumentException e) {
            final ExecutionReport rejected = createRejectedExecutionReport(message, e.getMessage());
            sendMessage(rejected, sessionID);
        }
    }

    private ExecutionReport createPendingNewExecutionReport(NewOrderSingle message)
        throws FieldNotFound, IllegalArgumentException {
        final String clOrdId = message.getClOrdID().getValue();
        final char side = message.getSide().getValue();
        final String symbol = message.getSymbol().getValue();
        if (!"AAPL".equals(symbol)) {
            throw new IllegalArgumentException("Symbol must be AAPL");
        }
        final double orderQty = message.getOrderQty().getValue();
        if (orderQty <= 0d) {
            throw new IllegalArgumentException("Order Qty must be greater than zero");
        }
        final double price = message.getPrice().getValue();
        if (price <= 0d) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        final String account = message.getAccount().getValue();
        if (!"1234".equals(account)) {
            throw new IllegalArgumentException("Account must be 1234");
        }
        final char ordType = message.getOrdType().getValue();
        if (OrdType.LIMIT != ordType) {
            throw new IllegalArgumentException("OrdType must be LIMIT");
        }
        final char timeInForce = message.getTimeInForce().getValue();
        if (TimeInForce.FILL_OR_KILL != timeInForce) {
            throw new IllegalArgumentException("TimeInForce must be FILL_OR_KILL");
        }
        ExecutionReport executionReport = new ExecutionReport(
            new OrderID(clOrdId),
            new ExecID(UUID.randomUUID().toString()),
            new ExecType(ExecType.PENDING_NEW),
            new OrdStatus(OrdStatus.PENDING_NEW),
            new Side(side),
            new LeavesQty(orderQty),
            new CumQty(0d),
            new AvgPx(0d)
        );
        executionReport.set(new ClOrdID(clOrdId));
        executionReport.set(new Account(account));
        executionReport.set(new Symbol(symbol));
        executionReport.set(new OrderQty(orderQty));
        executionReport.set(new Price(price));
        executionReport.set(new OrdType(ordType));
        executionReport.set(new TimeInForce(timeInForce));
        executionReport.set(new TransactTime());
        return executionReport;
    }

    private ExecutionReport createRejectedExecutionReport(NewOrderSingle message, String reason)
        throws FieldNotFound {
        final String clOrdId = message.getClOrdID().getValue();
        final char side = message.getSide().getValue();
        final double orderQty = message.getOrderQty().getValue();
        final String symbol = message.getSymbol().getValue();
        ExecutionReport executionReport = new ExecutionReport(
            new OrderID(clOrdId),
            new ExecID(UUID.randomUUID().toString()),
            new ExecType(ExecType.REJECTED),
            new OrdStatus(OrdStatus.REJECTED),
            new Side(side),
            new LeavesQty(orderQty),
            new CumQty(0d),
            new AvgPx(0d)
        );
        executionReport.set(new Symbol(symbol));
        executionReport.set(new Text(reason));
        executionReport.set(new TransactTime());
        return executionReport;
    }

    private void sendMessage(Message message, SessionID sessionID) {
        try {
            Session.sendToTarget(message, sessionID);
        } catch (SessionNotFound e) {
            throw new RuntimeException(e);
        }
    }

}
