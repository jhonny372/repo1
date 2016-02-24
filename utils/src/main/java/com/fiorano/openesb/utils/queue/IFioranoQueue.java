/**
 * Copyright (c) 1999-2007, Fiorano Software Technologies Pvt. Ltd. and affiliates.
 * Copyright (c) 2008-2015, Fiorano Software Pte. Ltd. and affiliates.
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Fiorano Software ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * enclosed with this product or entered into with Fiorano.
 */


package com.fiorano.openesb.utils.queue;

import java.util.Enumeration;

/**
   Defines the semantics of a Queue implementation
 */
public interface IFioranoQueue
{
    /**
       @roseuid 35EC533800B2
     */
    public void push(Object data);

    /**
       @roseuid 35EC53480000
     */
    public Object pop();

    /**
       @roseuid 35EC534B039E
     */
    public void pushWithNotify(Object data);

    /**
       @roseuid 35EC535301F1
     */
    public Object popWithWait(long timeout);

    public Object popWithWait(long timeout, ICallState callee);

    /**
     *    Adds all elements of a FioranoQueueImpl to this Object.
     */
    public void addQueue(IFioranoQueue toAdd);

    /**
       @roseuid 35EC54470300
     */
    public Enumeration elements();

    /**
       @roseuid 35ED91B500C7
     */
    public int getSize();

    /**
       @roseuid 35ED91C2029D
     */
    public boolean remove(Object toRemove);

    /**
       @remove all nodes in this Queue.
     */
    public void clear();
}