/*
 * Copyright (c) Fiorano Software Pte. Ltd. and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.openesb.utils;

import java.io.*;

public class MarkableInputStream extends FilterInputStream {
    private byte buff[];

    private int readLimit = 0;
    private int buffOffset = 0;
    private boolean buffering = false;

    public MarkableInputStream(InputStream in){
        super(in);
    }

    // strectch buffer so that we can place count bytes in buffer
    private void stretchBuffer(int count){
        if(buffOffset+count>buff.length){ // we can't place count bytes in buffer
            byte newBuff[] = new byte[Math.min(Math.max(buffOffset+count, buff.length+64), readLimit)+1];
            System.arraycopy(buff, 0, newBuff, 0, buffOffset);
            buff = newBuff;
        }
    }

    public int read() throws IOException {
        if(buff!=null){
            if(buffering){
                int b = super.read();
                if(buffOffset<readLimit){ // buffer has space
                    stretchBuffer(1);
                    buff[buffOffset++] = (byte)b;
                }else // user read beyond readlimit
                    buff = null;
                return b;
            }else{
                if(buffOffset<buff.length) // buffer has unread data
                    return buff[buffOffset++];
                else // buffer completely read
                    buff = null;
            }
        }
        return super.read();
    }

    public int read(byte b[], int off, int len) throws IOException{
        if(buff!=null){
            if(buffering){
                int count = super.read(b, off, len);
                if(buffOffset+count<readLimit){ // buffer has space
                    stretchBuffer(count);
                    System.arraycopy(b, off, buff, buffOffset, count);
                    buffOffset+=count;
                }else // user read beyond readlimit
                    buff = null;
                return count;
            }else{
                if(buffOffset<buff.length){ // buffer has unread data
                    int count = Math.min(len, buff.length - buffOffset);
                    System.arraycopy(buff, buffOffset, b, off, count);
                    buffOffset+=count;
                    return count;
                }else // buffer completely read
                    buff = null;
            }
        }
        return super.read(b, off, len);
    }

    public long skip(long n) throws IOException{
        throw new UnsupportedOperationException();
    }

    public synchronized void mark(int readLimit){
        if(buff!=null && !buffering)
            super.in = new SequenceInputStream(new ByteArrayInputStream(buff, buffOffset, buff.length-buffOffset), super.in);

        this.readLimit = readLimit;
        buff = new byte[Math.min(64, readLimit)];
        buffOffset = 0;
        buffering = true;
    }

    public synchronized void reset() throws IOException{
        if(buff==null)
            throw new IOException("reset called without mark!!!");
        if(buffOffset<buff.length){ // shrink buffer to exact fit
            byte newBuff[] = new byte[buffOffset];
            System.arraycopy(buff, 0, newBuff, 0, buffOffset);
            buff = newBuff;
        }
        buffOffset = 0;
        buffering = false;
    }

    public boolean markSupported(){
        return true;
    }
}

