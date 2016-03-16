package com.fiorano.openesb.route.impl;

import com.fiorano.openesb.route.RouteOperationHandler;
import com.fiorano.openesb.transport.Message;
import com.fiorano.openesb.transport.TransportService;
import com.fiorano.openesb.transport.impl.jms.JMSMessage;
import com.fiorano.openesb.transport.impl.jms.JMSMessageConfiguration;
import com.fiorano.openesb.transport.impl.jms.JMSPort;


import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Enumeration;

/**
 * Created by root on 3/7/16.
 */
public class MessageCreationHandler implements RouteOperationHandler<Message<javax.jms.Message>> {

    private TransportService<JMSPort,JMSMessage> transportService;

    public MessageCreationHandler(MessageCreationConfiguration  messageCreationConfiguration) {
        this.transportService = messageCreationConfiguration.getTransportService();
    }

    @Override
    public void handleOperation(Message<javax.jms.Message> message) throws Exception {
        JMSMessage messageToClone = transportService.createMessage(new JMSMessageConfiguration(getJmsType(message)));
        javax.jms.Message writeableMessage = messageToClone.getMessage();
        javax.jms.Message readOnlyMessage = message.getMessage();
        clone(readOnlyMessage,writeableMessage);
        message.setInternalMessage(writeableMessage);
    }

    private void clone(javax.jms.Message readOnlyMessage, javax.jms.Message writeableMessage) {
        try {
            Enumeration<String> propertyNames = readOnlyMessage.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String s = propertyNames.nextElement();
                Object o = readOnlyMessage.getObjectProperty(s);
                if (o instanceof java.lang.Boolean) {
                    writeableMessage.setBooleanProperty(s, readOnlyMessage.getBooleanProperty(s));
                } else if (o instanceof java.lang.Byte) {
                    writeableMessage.setByteProperty(s, readOnlyMessage.getByteProperty(s));
                } else if (o instanceof java.lang.Double) {
                    writeableMessage.setDoubleProperty(s, readOnlyMessage.getDoubleProperty(s));
                } else if (o instanceof java.lang.String) {
                    writeableMessage.setStringProperty(s, readOnlyMessage.getStringProperty(s));
                } else if (o instanceof java.lang.Float) {
                    writeableMessage.setFloatProperty(s, readOnlyMessage.getFloatProperty(s));
                } else if (o instanceof java.lang.Integer) {
                    writeableMessage.setIntProperty(s, readOnlyMessage.getIntProperty(s));
                } else if (o instanceof java.lang.Long) {
                    writeableMessage.setLongProperty(s, readOnlyMessage.getLongProperty(s));
                } else if (o instanceof java.lang.Short) {
                    writeableMessage.setShortProperty(s, readOnlyMessage.getShortProperty(s));
                } else {
                    writeableMessage.setObjectProperty(s, readOnlyMessage.getObjectProperty(s));
                }
            }
            if (readOnlyMessage instanceof TextMessage) {
                ((TextMessage) writeableMessage).setText(((TextMessage) readOnlyMessage).getText());
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private JMSMessageConfiguration.MessageType getJmsType(Message<javax.jms.Message> message) throws JMSException {
        if(message.getMessage().getJMSType()==null){
            return JMSMessageConfiguration.MessageType.Text;
        }
        return JMSMessageConfiguration.MessageType.valueOf(message.getMessage().getJMSType());
    }
}
