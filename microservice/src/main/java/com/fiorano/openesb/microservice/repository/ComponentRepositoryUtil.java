/*
 * Copyright (c) Fiorano Software Pte. Ltd. and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.openesb.microservice.repository;

import com.fiorano.openesb.utils.FileUtil;
import com.fiorano.openesb.utils.exception.FioranoException;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class ComponentRepositoryUtil {
        public static void copyStream(InputStream in, OutputStream out)
        throws FioranoException {
        try {
            BufferedInputStream bis = new BufferedInputStream(in);
            BufferedOutputStream bos = new BufferedOutputStream(out);

            while (true)
            {
                byte[] bytes = new byte[MicroServiceConstants.FILE_COPY_CHUNK_SIZE];

                int numRead = bis.read(bytes);
                if (numRead < 0)
                    break;

                if (numRead < MicroServiceConstants.FILE_COPY_CHUNK_SIZE)
                {
                    byte[] toWrite = new byte[numRead];
                    System.arraycopy(bytes, 0, toWrite, 0, numRead);
                    bos.write(toWrite);
                    continue;  // can stop since it wudve been end of file
                }

                bos.write(bytes);
            }

            bis.close();
            bos.close();
        } catch (IOException e) {
            throw new FioranoException(e);
        }
    }

        /**
         *  Writes the specified data to the specified fie
         *
         * @param file Description of the Parameter
         * @param fileData Description of the Parameter
         * @exception java.io.IOException Description of the Exception
         * @exception java.io.FileNotFoundException Description of the Exception
         */
    public static void writeFileData(File file, byte[] fileData)
            throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bos.write(fileData);
        bos.close();
    }

    /**
     *  Compares two string for equality. If both are null, returns true. Wild
     *  card (*) can also be used
     *
     * @param str1 The source string which is to be compared
     * @param str2 The string from which comparison is to be done
     * @return Description of the Return Value
     */
    public static boolean compare(String str1, String str2)
    {
//        if (str1 != null)
//            str1 = str1.toUpperCase();
//        if (str2 != null)
//            str2 = str2.toUpperCase();
        // if the string to be compared is null or empty or it has *
        // then return true
        if (str1 == null || str1.trim().equals("") || str1.equals("*"))
            return true;

        // if the string contains *, then check for the substring from index
        // 0 to the index of * and from * to the end of string
        if (str1.indexOf("*") >= 0)
        {
            boolean partBefore = true;
            boolean partAfter = true;

            // check for the part before *
            if (str1.indexOf("*") > 0)
            {
                String str1Substring1 = str1.substring(0, str1.indexOf("*"));

                if (!str2.startsWith(str1Substring1))
                    partBefore = false;
            }

            // check for the part after *
            String str1Substring2 = str1.substring(str1.indexOf("*") + 1, str1.length());

            if (!str2.endsWith(str1Substring2))
                partAfter = false;
            return partAfter && partBefore;
        }

        // The string doesn't have wild cards and it is not null. So,
        // simply compare the strings and return the result
        if (str2 == null)
            return false;
        else
        {
            int compResult = str1.compareTo(str2);

            return compResult == 0;
        }
    }

    /**
     * Gets the bytes from the file
     * @param file FileObject
     * @return byte[]. If it returns NULL => the file had 0 bytes in it and This should be logged at the place where this method is called.
     * @throws FioranoException
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File file)
            throws FioranoException, IOException
    {
        if(!file.exists())
            return null;
//        throw new FioranoException(ComponentErrorCodes.RESOURCE_FILE_NOTPRESENT,
//                    LogHelper.getErrMessage(ILogModule.SERVICE_REPOSITORY,16 ,file.getName().getPath()));
        try
        {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            int numBytes = bis.available();

            if (numBytes <= 0)
            {
                bis.close();
                //LogHelper.logErr(ILogModule.SERVICE_REPOSITORY, 17,file.getName().getPath());
                //todo must LOG AT HIGHER LEVEL important
                return null;
            }

            byte[] b = new byte[numBytes];

            int n = bis.read(b);
            if(n!=numBytes)
                throw new FioranoException("bytes number mismatch");
            //LogHelper.getErrMessage(ILogModule.SERVICE_REPOSITORY,15 ,file.getName().getPath()));

            bis.close();
            return b;
        }
        catch (IOException e)
        {
            //if (TifTrace.ServiceRepository > TraceLevels.Debug)
            //    e.printStackTrace(); -->todo :log at higher level.

            throw new FioranoException(e);
            //LogHelper.getErrMessage(ILogModule.SERVICE_REPOSITORY,15 ,file.getName().getPath()),e);
        }

    }

    /**
     * Gets the bytes from the file
     * @param in InputStream
     * @return byte[]. If it returns NULL => the file had 0 bytes in it and This should be logged at the place where this method is called.
     * @throws FioranoException
     * @throws IOException
     */
    public static byte[] getBytesFromInputStream(InputStream in, String nameSpace, String locationHint) throws FioranoException{
        try
        {
            BufferedInputStream bis = new BufferedInputStream(in);
            int numBytes = bis.available();

            if (numBytes <= 0)
            {
                bis.close();
                //LogHelper.logErr(ILogModule.SERVICE_REPOSITORY, 17,file.getName().getPath());
                //todo must LOG AT HIGHER LEVEL important
                return null;
            }

            byte[] b = new byte[numBytes];

            int n = bis.read(b);
            if(n!=numBytes)
                throw new FioranoException();

            bis.close();
            return b;
        }
        catch (IOException e)
        {
            //if (TifTrace.ServiceRepository > TraceLevels.Debug)
            //    e.printStackTrace(); -->todo :log at higher level.

            throw new FioranoException(e);
            //LogHelper.getErrMessage(ILogModule.SERVICE_REPOSITORY,15 ,file.getName().getPath()),e);
        }
    }


    public static String resolve(String home, String relativePath, Map/*<String, File>*/ favorites){
        if(File.separatorChar!='\\')
            relativePath = relativePath.replace('\\', File.separatorChar);
        if(File.separatorChar!='/')
            relativePath = relativePath.replace('/', File.separatorChar);
        Iterator iter = favorites.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            String key = '$'+(String)entry.getKey()+'$';
            if(relativePath.startsWith(key))
                return entry.getValue()+relativePath.substring(key.length());
        }

        return home + File.separator + relativePath;
    }

    public static void moveChildrenToTgtFolder(File src, File tgt) throws FioranoException
    {
        if(!src.isDirectory() || (tgt.exists() && !tgt.isDirectory())) {
            throw new FioranoException("source target mismatch");
        }

        File[] childlist = src.listFiles();
        for(int i=0; i<childlist.length; i++)
        {
            File srcChild = childlist[i];
            File tgtChild = new File(tgt,srcChild.getName());
            srcChild.renameTo(tgtChild);
        }
    }

    public static void copyChildrenToTgtFolder(File src, File tgt)
            throws FioranoException
    {
        if( (!src.isDirectory()  || (tgt.exists() && !tgt.isDirectory()) )) {
            throw new FioranoException("Source target mismatch");
        }

        File[] childlist = src.listFiles();
        for(int i=0; i<childlist.length; i++)
        {
            File srcChild = childlist[i];
            File tgtChild = new File(tgt, srcChild.getName());
            try {
                FileUtil.copyFileUsingIO(srcChild, tgtChild);
            } catch (IOException e) {
                throw new FioranoException(e);
            }

        }
    }

}
