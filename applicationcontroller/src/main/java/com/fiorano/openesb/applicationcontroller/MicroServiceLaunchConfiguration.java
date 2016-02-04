package com.fiorano.openesb.applicationcontroller;

import com.fiorano.openesb.application.application.ServiceInstance;
import com.fiorano.openesb.application.service.ServiceRef;
import com.fiorano.openesb.microservice.launch.AdditionalConfiguration;
import com.fiorano.openesb.microservice.launch.LaunchConfiguration;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class MicroServiceLaunchConfiguration implements LaunchConfiguration {

    String userName;
    String password;
    List runtimeArgs;
    long stopRetryInterval;
    int numberOfStopAttempts;
    String microserviceId;
    String microserviceVersion;
    String name;
    String applicationName;
    String applicationVersion;
    AdditionalConfiguration additionalConfiguration;
    LaunchMode launchMode;
    List logModules;
    Vector runtimeDependencies;

    MicroServiceLaunchConfiguration(String appGuid, String appVersion, String userName, String password, ServiceInstance si){
        this.userName = userName;
        this.password = password;
        this.runtimeArgs = si.getRuntimeArguments();
        this.microserviceId = si.getGUID();
        this.name = si.getName();
        this.microserviceVersion = String.valueOf(si.getVersion());
        this.applicationName = appGuid;
        this.applicationVersion = appVersion;
        int i = si.getLaunchType();
        if(i==1){
            this.launchMode = LaunchMode.SEPARATE_PROCESS;
        }else if (i==2){
            this.launchMode = LaunchMode.IN_MEMORY;
        }else if (i==3){
            this.launchMode = LaunchMode.DOCKER;
        }
        this.logModules = si.getLogModules();
        Iterator _enum = (si.getServiceRefs()).iterator();
        while(_enum!=null && _enum.hasNext()){
            ServiceRef runtimeDependency = (ServiceRef)_enum.next();
            addRuntimeDependency(runtimeDependency);
        }

    }
    public void addRuntimeDependency(ServiceRef servDependencyInfo)
    {
        if (runtimeDependencies == null)
        {
            runtimeDependencies = new Vector();
        }
        if (!runtimeDependencies.contains(servDependencyInfo))
            runtimeDependencies.add(servDependencyInfo);
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public List getRuntimeArgs() {
        return runtimeArgs;
    }

    public List getLogModules() {
        return logModules;
    }

    public Enumeration<ServiceRef> getRuntimeDependencies() {
        if(runtimeDependencies!=null){
            return runtimeDependencies.elements();
        }
        return null;
    }

    public LaunchMode getLaunchMode() {
        return launchMode;
    }

    public long getStopRetryInterval() {
        return 0;
    }

    public int getNumberOfStopAttempts() {
        return 0;
    }

    public String getMicroserviceId() {
        return microserviceId;
    }

    public String getMicroserviceVersion() {
        return microserviceVersion;
    }

    public String getName() {
        return name;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public AdditionalConfiguration getAdditionalConfiguration() {
        return additionalConfiguration;
    }
}