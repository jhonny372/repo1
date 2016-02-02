/**
 * Copyright (c) 1999-2007, Fiorano Software Technologies Pvt. Ltd. and affiliates.
 * Copyright (c) 2008-2014, Fiorano Software Pte. Ltd. and affiliates.
 * <p>
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information
 * of Fiorano Software ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * enclosed with this product or entered into with Fiorano.
 * <p>
 * Created by chaitanya on 13-01-2016.
 */

/**
 * Created by chaitanya on 13-01-2016.
 */
package com.fiorano.openesb.microservice.launch;


import java.util.List;

public interface LaunchConfiguration<A> {
    String getUserName();
    String getPassword();

    List getRuntimeArgs();

    enum LaunchMode {SEPARATE_PROCESS, IN_MEMORY, DOCKER}
    LaunchMode getLaunchMode();
    long getStopRetryInterval();
    int getNumberOfStopAttempts();
    String getMicroserviceId();
    String getMicroserviceVersion();
    String getName();
    String getApplicationName();
    String getApplicationVersion();
    A getAdditionalConfiguration();
}
