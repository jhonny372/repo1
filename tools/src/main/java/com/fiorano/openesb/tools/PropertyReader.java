package com.fiorano.openesb.tools;

import com.fiorano.openesb.microservice.launch.LaunchConstants;
import com.fiorano.openesb.rmiconnector.api.ServiceException;
import com.fiorano.openesb.utils.StringUtil;
import com.fiorano.openesb.utils.exception.FioranoException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Properties;

public class PropertyReader {
    private ServiceManualLauncher ServiceManualLauncher;

    public PropertyReader(ServiceManualLauncher ServiceManualLauncher) {
        this.ServiceManualLauncher = ServiceManualLauncher;
    }

    public Properties read(String propertiesFile) throws FioranoException {
        Properties prop = new Properties();
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(new File(propertiesFile));
            prop.load(inStream);
            String componentRepositoryPath = getComponentRepositoryPath(prop.getProperty(LaunchConstants.NODE_NAME));
            if (componentRepositoryPath != null) {
                ServiceManualLauncher.setComponentsPath(componentRepositoryPath);
            }
            prop.setProperty(LaunchConstants.COMPONENT_REPO_PATH, ServiceManualLauncher.COMP_REPOSITORY_PATH);
        } catch (IOException e) {
            throw new FioranoException("ERROR : Failed to load launch script. The properties file given is not specified or Invalid", e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                } finally {
                    inStream = null;
                }
            }
        }
        return prop;
    }

    private String getComponentRepositoryPath(String nodeName) {
        try {
            if (!StringUtil.isEmpty(nodeName)) {
                RmiClient rmiClient = ServiceManualLauncher.getConnectionManager();
                if (rmiClient == null) {
                    System.out.println("WARNING: Unable to fetch the component repository path. Reason: Could not connect to Enterprise server.");
                    System.out.println("Using default value " + ServiceManualLauncher.COMP_REPOSITORY_PATH + ". Update the path in scriptgen.[bat|sh] to appropriate value if required and launch");
                    return null;
                } else {
                    return rmiClient.getMicroServiceManager().getServiceRepositoryPath();
                }
            }
        } catch (RemoteException e) {
            System.out.println("WARNING: Unable to fetch the component repository path. Reason: " + e.getMessage());
            System.out.println("Using default value " + ServiceManualLauncher.COMP_REPOSITORY_PATH + ". Update the path in scriptgen.[bat|sh] to appropriate value if required and launch");
            e.printStackTrace();
        } catch (ServiceException e) {
            System.out.println("WARNING: Unable to fetch the component repository path. Reason: " + e.getMessage());
            System.out.println("Using default value " + ServiceManualLauncher.COMP_REPOSITORY_PATH + ". Update the path in scriptgen.[bat|sh] to appropriate value if required and launch");
            e.printStackTrace();
        }
        return null;
    }
}
