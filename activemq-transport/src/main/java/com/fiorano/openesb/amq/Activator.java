
package com.fiorano.openesb.amq;

import com.fiorano.openesb.transport.ConnectionProvider;
import com.fiorano.openesb.transport.TransportService;
import com.fiorano.openesb.transport.impl.jms.JMSConnectionConfiguration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Collection;
import java.util.Hashtable;

public class Activator implements BundleActivator {

    private AMQTransportService service;
    private Logger logger;

    public Activator() {
        logger = LoggerFactory.getLogger(getClass());
    }

    public void start(BundleContext context) throws Exception {
        System.out.println("Starting Active MQ Transport");
        try {
            service = new AMQTransportService();
        } catch (JMSException e) {
            System.out.println("Could not connect to MQ Server.");
            context.getBundle(0).stop();
        }
        context.registerService(TransportService.class, service, new Hashtable<String,Object>());
        System.out.println("Started Active MQ Transport");
    }

    public void stop(BundleContext context) {
        try {
            service.stop();
        } catch (Exception e) {
            logger.debug("Error stopping Active MQ Transport " + e.getMessage());
        }
        System.out.println("Stopped Active MQ Transport");
    }

}