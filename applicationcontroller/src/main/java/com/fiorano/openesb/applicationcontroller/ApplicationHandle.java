package com.fiorano.openesb.applicationcontroller;

import com.fiorano.openesb.application.BreakpointMetaData;
import com.fiorano.openesb.application.application.*;
import com.fiorano.openesb.application.application.Route;
import com.fiorano.openesb.application.aps.ApplicationStateDetails;
import com.fiorano.openesb.application.aps.ServiceInstanceStateDetails;
import com.fiorano.openesb.jmsroute.impl.JMSRouteConfiguration;
import com.fiorano.openesb.microservice.launch.MicroServiceRuntimeHandle;
import com.fiorano.openesb.microservice.launch.impl.MicroServiceLauncher;
import com.fiorano.openesb.route.*;
import com.fiorano.openesb.route.impl.*;
import com.fiorano.openesb.transport.TransportService;
import com.fiorano.openesb.transport.impl.jms.JMSPortConfiguration;
import com.fiorano.openesb.utils.exception.FioranoException;

import java.util.*;

public class ApplicationHandle {

    private Application application;
    private MicroServiceLauncher service;
    private RouteService<RouteConfiguration> routeService;
    private TransportService transport;
    Map<String, MicroServiceRuntimeHandle> microServiceHandleList = new HashMap<>();
    private Map<String, com.fiorano.openesb.route.Route> routeMap = new HashMap<>();
    private Map<String, BreakpointMetaData> breakpoints = new HashMap<String, BreakpointMetaData>();
    private Map<String, BreakpointMetaData> pendingBreakpointsForClouser = new HashMap<String, BreakpointMetaData>();
    private ApplicationController applicationController;


    Vector<String> pendingQueueIDs;
    //hash table of handle IDs of routes on which debuggers are set.[key -routeID Vs object -handleID]
    private Hashtable debugHandleIDs = new Hashtable();

    //  Unique AppGUID.
    private String appGUID;

    //  version.
    private float version;

    private String environmentLabel;

    //  LaunchTime.
    private long launchTime;

    //  KillTime.
    private long killTime = -1;

    // List of RouteGUID's in the application
    //private Vector<String> debugrouteGUIDS;

    private String passwd;

    private String userName;

    public ApplicationHandle(ApplicationController applicationController, Application application, MicroServiceLauncher service, RouteService<RouteConfiguration> routeService, TransportService transport, String userName, String passwd){
        this.applicationController = applicationController;
        this.application = application;
        this.service = service;
        this.routeService = routeService;
        this.transport = transport;
        this.appGUID = application.getGUID();
        this.version = application.getVersion();
        this.launchTime = System.currentTimeMillis();
        this.environmentLabel = application.getLabel();
        this.userName = userName;
        this.passwd = passwd;
    }

    public Application getApplication(){
        return application;
    }

    public MicroServiceLauncher getService() {
        return service;
    }

    public void setService(MicroServiceLauncher service) {
        this.service = service;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public String getAppGUID() {
        return appGUID;
    }

    public void setAppGUID(String appGUID) {
        this.appGUID = appGUID;
    }

    public void createRoutes() throws Exception {
        for(final Route route: application.getRoutes()) {

            String sourcePortInstance = route.getSourcePortInstance();
            JMSPortConfiguration sourceConfiguration = new JMSPortConfiguration();
            String sourceServiceInstance = route.getSourceServiceInstance();
            sourceConfiguration.setName(getPortName(sourcePortInstance, sourceServiceInstance));
            OutputPortInstance outputPortInstance = application.getServiceInstance(sourceServiceInstance).getOutputPortInstance(sourcePortInstance);

            int type = outputPortInstance.getDestinationType();
            sourceConfiguration.setPortType(type == PortInstance.DESTINATION_TYPE_QUEUE ?
                    JMSPortConfiguration.PortType.QUEUE : JMSPortConfiguration.PortType.TOPIC);

            String destPortInstance = route.getTargetPortInstance();
            JMSPortConfiguration destinationConfiguration = new JMSPortConfiguration();
            String targetServiceInstance = route.getTargetServiceInstance();
            destinationConfiguration.setName(getPortName(destPortInstance, targetServiceInstance));
            InputPortInstance inputPortInstance = application.getServiceInstance(targetServiceInstance).getInputPortInstance(destPortInstance);
            int inputPortInstanceDestinationType = inputPortInstance.getDestinationType();
            destinationConfiguration.setPortType(inputPortInstanceDestinationType == PortInstance.DESTINATION_TYPE_QUEUE ?
                    JMSPortConfiguration.PortType.QUEUE : JMSPortConfiguration.PortType.TOPIC);
            JMSRouteConfiguration routeConfiguration = new JMSRouteConfiguration(sourceConfiguration, destinationConfiguration);

            MessageCreationConfiguration messageCreationConfiguration = new MessageCreationConfiguration();
            messageCreationConfiguration.setTransportService(transport);
            routeConfiguration.getRouteOperationConfigurations().add(messageCreationConfiguration);

            CarryForwardContextConfiguration carryForwardContextConfiguration = new CarryForwardContextConfiguration();
            carryForwardContextConfiguration.setApplication(application);
            carryForwardContextConfiguration.setInputPortInstance(inputPortInstance);
            carryForwardContextConfiguration.setServiceInstanceName(sourceServiceInstance);
            routeConfiguration.getRouteOperationConfigurations().add(carryForwardContextConfiguration);

            Transformation applicationContextTransformation = outputPortInstance.getApplicationContextTransformation();
            if(applicationContextTransformation != null) {
                TransformationConfiguration transformationConfiguration = new TransformationConfiguration();
                transformationConfiguration.setXsl(applicationContextTransformation.getScript());
                transformationConfiguration.setTransformerType(applicationContextTransformation.getFactory());
                transformationConfiguration.setJmsXsl(applicationContextTransformation.getJMSScript());
                routeConfiguration.getRouteOperationConfigurations().add(transformationConfiguration);
            }

            if(route.getSenderSelector()!=null){
                SenderSelectorConfiguration senderSelectorConfiguration = new SenderSelectorConfiguration();
                senderSelectorConfiguration.setSourceName(route.getSenderSelector());
                senderSelectorConfiguration.setAppName_version(application.getGUID()+":"+application.getVersion());
                routeConfiguration.getRouteOperationConfigurations().add(senderSelectorConfiguration);
            }

            if(route.getApplicationContextSelector() != null) {
                XmlSelectorConfiguration appContextSelectorConfig = new XmlSelectorConfiguration("AppContext");
                appContextSelectorConfig.setXpath(route.getApplicationContextSelector().getXPath());
                appContextSelectorConfig.setNsPrefixMap(route.getApplicationContextSelector().getNamespaces());
                routeConfiguration.getRouteOperationConfigurations().add(appContextSelectorConfig);
            }

            if(route.getBodySelector() != null) {
                XmlSelectorConfiguration bodySelectorConfig = new XmlSelectorConfiguration("Body");
                bodySelectorConfig.setXpath(route.getBodySelector().getXPath());
                bodySelectorConfig.setNsPrefixMap(route.getBodySelector().getNamespaces());
                routeConfiguration.getRouteOperationConfigurations().add(bodySelectorConfig);
            }

            if(route.getMessageTransformation()!=null) {
                TransformationConfiguration transformationConfiguration = new TransformationConfiguration();
                transformationConfiguration.setXsl(route.getMessageTransformation().getScript());
                transformationConfiguration.setTransformerType(route.getMessageTransformation().getFactory());
                transformationConfiguration.setJmsXsl(route.getMessageTransformation().getJMSScript());
                routeConfiguration.getRouteOperationConfigurations().add(transformationConfiguration);
            }


            com.fiorano.openesb.route.Route route1 = routeService.createRoute(routeConfiguration);
            route1.start();
            routeMap.put(route.getName(), route1);
        }
    }

    private String getPortName(String portInstance, String sourceServiceInstance) {
        return application.getGUID() + "__" + application.getVersion() + "__" + sourceServiceInstance + "__" + portInstance;
    }

    public void startAllMicroServices() {
        for (ServiceInstance instance : application.getServiceInstances()) {
            String instanceName = instance.getName();
            MicroServiceLaunchConfiguration mslc = new MicroServiceLaunchConfiguration(application.getGUID(), String.valueOf(application.getVersion()), "karaf", "karaf", instance);
            try {
                microServiceHandleList.put(instanceName, service.launch(mslc,instance.getConfiguration()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopApplication() throws Exception {
        for(ServiceInstance serviceInstance : application.getServiceInstances()) {
            for(PortInstance portInstance : serviceInstance.getInputPortInstances()) {
                JMSPortConfiguration portConfiguration = getPortConfiguration(serviceInstance, portInstance);
                transport.disablePort(portConfiguration);
            }
            for(PortInstance portInstance : serviceInstance.getOutputPortInstances()) {
                JMSPortConfiguration portConfiguration = getPortConfiguration(serviceInstance, portInstance);
                transport.disablePort(portConfiguration);
            }
        }
        for(MicroServiceRuntimeHandle handle:microServiceHandleList.values()){
            handle.stop();
        }
        for(com.fiorano.openesb.route.Route route :routeMap.values()) {
            route.stop();
        }
    }

    private JMSPortConfiguration getPortConfiguration(ServiceInstance serviceInstance, PortInstance portInstance) {
        JMSPortConfiguration portConfiguration = new JMSPortConfiguration();
        int type = portInstance.getDestinationType();
        portConfiguration.setPortType(type == PortInstance.DESTINATION_TYPE_QUEUE ?
                JMSPortConfiguration.PortType.QUEUE : JMSPortConfiguration.PortType.TOPIC);
        portConfiguration.setName(getPortName(portInstance.getName(), serviceInstance.getName()));
        return portConfiguration;
    }

    public void addBreakPoint(String routeName) throws Exception {
        com.fiorano.openesb.route.Route route = routeMap.get(routeName);
        if(route==null){
            throw new FioranoException("Route with name: "+routeName+" does not exist in the Application: " + application.getGUID());
        }

        JMSPortConfiguration destinationConfiguration = new JMSPortConfiguration();
        destinationConfiguration.setName(application.getGUID()+"__"+application.getVersion()+routeName+"__BP");
        destinationConfiguration.setPortType(JMSPortConfiguration.PortType.QUEUE);
        route.changeTargetDestination(destinationConfiguration);
    }

    public void removeBreakPoint(String routeName) throws Exception{
        com.fiorano.openesb.route.Route route = routeMap.get(routeName);
        List<Route> routes = application.getRoutes();
        Route routeInfo = null;
        for(Route r:routes){
            if(r.getName().equals(routeName)){
                routeInfo = r;
            }
        }
        if(routeInfo==null){
            throw new FioranoException("Route info with name: "+routeName+" does not exist in the Application: " + application.getGUID());
        }

        String appKey = application.getGUID() + "__" + application.getVersion() + "__";
        String destPortInstance = routeInfo.getTargetPortInstance();
        JMSPortConfiguration destinationConfiguration = new JMSPortConfiguration();
        String targetServiceInstance = routeInfo.getTargetServiceInstance();
        destinationConfiguration.setName(appKey + targetServiceInstance + "__" +destPortInstance);
        InputPortInstance inputPortInstance = application.getServiceInstance(targetServiceInstance).getInputPortInstance(destPortInstance);
        int inputPortInstanceDestinationType = inputPortInstance.getDestinationType();
        destinationConfiguration.setPortType(inputPortInstanceDestinationType == PortInstance.DESTINATION_TYPE_QUEUE ?
                JMSPortConfiguration.PortType.QUEUE : JMSPortConfiguration.PortType.TOPIC);
        route.changeTargetDestination(destinationConfiguration);
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void stopAllMicroServices() throws FioranoException{
        for(MicroServiceRuntimeHandle handle:microServiceHandleList.values()){
            try {
                handle.stop();
            } catch (Exception e) {
                throw new FioranoException(e);
            }
        }
    }

    public void startMicroService(String microServiceName) throws FioranoException {
        ServiceInstance instance = application.getServiceInstance(microServiceName);
        MicroServiceLaunchConfiguration mslc = new MicroServiceLaunchConfiguration(application.getGUID(), String.valueOf(application.getVersion()), "karaf", "karaf", instance);
        try {
            microServiceHandleList.put(microServiceName, service.launch(mslc, instance.getConfiguration()));
        } catch (Exception e) {
            throw new FioranoException(e);
        }
    }

    public void stopMicroService(String microServiceName) throws FioranoException {
        if(!isMicroserviceRunning(microServiceName)){
            throw new FioranoException("Microservice is not running");
        }
        try {
            microServiceHandleList.get(microServiceName).stop();
        } catch (Exception e) {
            throw new FioranoException(e);
        }
    }

    public boolean isMicroserviceRunning(String microServiceName) {
        return microServiceHandleList.containsKey(microServiceName);
    }

    public String getLaunchMode(String name) {
        return microServiceHandleList.get(name).getLaunchMode().name();
    }


    public ApplicationStateDetails getApplicationDetails() throws FioranoException {


       // logger.debug(Bundle.class, Bundle.EXECUTING_CALL, "getApplicationDetails()");

        ApplicationStateDetails appDetails = new ApplicationStateDetails();

        appDetails.setAppGUID(appGUID);
        appDetails.setAppVersion(String.valueOf(application.getVersion()));
        appDetails.setKillTime(killTime);
        appDetails.setLaunchTime(launchTime);
        appDetails.setApplicationLabel(environmentLabel);

            for (String serviceName: microServiceHandleList.keySet()) {
                MicroServiceRuntimeHandle serviceHandle = microServiceHandleList.get(serviceName);
                if (serviceHandle == null)
                    continue;
                String instName = serviceHandle.getServiceInstName();
                ServiceInstanceStateDetails stateDetails = serviceHandle.getServiceStateDetails();
                appDetails.addServiceStatus(instName, stateDetails);
                String exceptionTrace = serviceHandle.getExceptionTrace();
                if (exceptionTrace != null)
                    appDetails.addServiceExceptionTrace(instName, exceptionTrace);
            }

        //  Get the details of External Services too.

        for (Object o : application.getRemoteServiceInstances()) {
            RemoteServiceInstance extInstance = (RemoteServiceInstance) o;
            String extAppGUID = extInstance.getApplicationGUID();
            ApplicationHandle extAppHandle = applicationController.getApplicationHandle(extAppGUID, extInstance.getApplicationVersion());

            if (extAppHandle == null) {
               // logger.error(Bundle.class, Bundle.APPHANDLE_NOT_PRESENT, appGUID+ITifosiConstants.APP_VERSION_DELIM+Float.toString(application.getVersion()));
                continue;
            }

            // If the External service is actually configured to a application of version different of that
            // of the one which is running. then do not proceed.
            if (extAppHandle.getApplication().getVersion() != extInstance.getApplicationVersion())
                continue;

            String extInstName = extInstance.getRemoteName();
            MicroServiceRuntimeHandle extServiceHandle = extAppHandle.getMicroServiceHandle(extInstName);

            if (extServiceHandle == null)
                continue;

            String localInstName = extInstance.getName();
            ServiceInstanceStateDetails stateDetails = extServiceHandle.getServiceStateDetails();

            appDetails.addServiceStatus(localInstName, stateDetails);

            String exceptionTrace = extServiceHandle.getExceptionTrace();

            if (exceptionTrace != null)
                appDetails.addServiceExceptionTrace(localInstName, exceptionTrace);
        }

        // add the routes with breakpoint to app state details

        if (breakpoints != null && breakpoints.size() > 0) {
            for (String aDebugrouteGUIDS : breakpoints.keySet()) {
                appDetails.addDebugRoute(aDebugrouteGUIDS);
            }
        }

        if (pendingBreakpointsForClouser != null && pendingBreakpointsForClouser.size() > 0) {
            for (String pendingDebugRouteGUID : pendingBreakpointsForClouser.keySet()) {
                appDetails.addPendingDebugRoutesForClosure(pendingDebugRouteGUID);
            }
        }
        return appDetails;
    }

    private MicroServiceRuntimeHandle getMicroServiceHandle(String serviceName){
        return microServiceHandleList.get(serviceName);
    }
}
