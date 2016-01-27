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

package com.fiorano.openesb.application;

import com.fiorano.openesb.utils.exception.FioranoException;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *  Carries Queue name(port name) vs last delivered indices Used while
 *  committing a Tifosi session.
 *
 * @author sachin
 * @created April 30, 2002
 * @version 2.0
 */
public class AckPacket extends DmiObject
{

    Hashtable       m_hashOfIndicesToCommit = new Hashtable();

    /**
     *  Constructor for the AckPacket object
     */
    public AckPacket()
    {
    }

    /**
     *  Gets the objectID attribute of the AckPacket object
     *
     * @return The objectID value
     */
    public int getObjectID()
    {
        return DmiObjectTypes.ACKNOWLEDGEMENT_PACKET;
    }

    /**
     *  Gets the ackInfo attribute of the AckPacket object
     *
     * @return The ackInfo value
     */
    public Hashtable getAckInfo()
    {
        return m_hashOfIndicesToCommit;
    }

    /**
     *  Description of the Method
     *
     * @exception FioranoException Description of the Exception
     */
    public void validate()
        throws FioranoException
    {
    }

    /**
     *  Description of the Method
     */
    public void reset()
    {
    }

    /**
     *  Adds a feature to the QueueInfoToAck attribute of the AckPacket object
     *
     * @param portName The feature to be added to the QueueInfoToAck attribute
     * @param index The feature to be added to the QueueInfoToAck attribute
     */
    public void addQueueInfoToAck(String portName, long index)
    {
        m_hashOfIndicesToCommit.put(portName, "" + index);
    }


    /**
     *  Description of the Method
     *
     * @param os Description of the Parameter
     * @param versionNo
     * @exception java.io.IOException Description of the Exception
     */
    public void toStream(java.io.DataOutput os, int versionNo)
        throws java.io.IOException
    {
        super.toStream(os, versionNo);

        os.writeInt(m_hashOfIndicesToCommit.size());

        Enumeration keys = m_hashOfIndicesToCommit.keys();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String val = (String) m_hashOfIndicesToCommit.get(key);

            writeUTF(os, key);
            writeUTF(os, val);
        }
    }


    /**
     *  Description of the Method
     *
     * @param is Description of the Parameter
     * @param versionNo
     * @exception java.io.IOException Description of the Exception
     */
    public void fromStream(java.io.DataInput is, int versionNo)
        throws java.io.IOException
    {
        super.fromStream(is, versionNo);

        int size = is.readInt();

        for (int i = 0; i < size; i++)
        {
            String key = readUTF(is);
            String val = readUTF(is);

            m_hashOfIndicesToCommit.put(key, val);
        }
    }

    /**
     *  Description of the Method
     *
     * @return Description of the Return Value
     */
    public int size()
    {
        return m_hashOfIndicesToCommit.size();
    }
}

