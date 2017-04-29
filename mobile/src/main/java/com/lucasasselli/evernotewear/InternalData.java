package com.lucasasselli.evernotewear;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class InternalData {

    // TODO Check if null

    private final static String TAG = "InternalData";

    public final static String OK = "OK";
    public final static String ERROR = "ERROR";

    Context context;

    InternalData(Context context){
        this.context = context;
    }

    public String saveInteger(int value, String path){
        return saveBytes(intToByteArray(value), path);
    }

    public String saveLong(long value, String path){
        return saveBytes(longToByteArray(value), path);
    }

    public String saveString(String value, String path){
        return saveBytes(value.getBytes(), path);
    }

    public int readInteger(String path, int defaultValue){
        byte temp[] = readBytes(path);
        if(temp!=null){
            return byteArrayToInt(temp);
        }else{
            return defaultValue;
        }
    }

    public long readLong(String path, long defaultValue){
        byte temp[] = readBytes(path);
        if(temp!=null){
            return byteArrayToLong(temp);
        }else{
            return defaultValue;
        }
    }

    public String readString(String path, String defaultValue){
        byte temp[] = readBytes(path);
        if(temp!=null){
            return new String(temp);
        }else{
            return defaultValue;
        }
    }

    public byte[] readBytes(String path){
        byte bytes[];

        try {
            FileInputStream inputStream = context.openFileInput(path);
            bytes = new byte[(int) inputStream.getChannel().size()];
            for(int i=0; i<bytes.length; i++){
               bytes[i] = (byte) (inputStream.read() & 0xFF);
            }
            inputStream.close();
        } catch(FileNotFoundException e){
            Log.e(TAG, "File not found", e);
            return null;
        } catch (IOException e){
            Log.e(TAG, "IO exception", e);
            return null;
        }

        return bytes;
    }

    private String saveBytes(byte bytes[], String path){
        try {
            OutputStream outputStream = context.openFileOutput(path, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();
        } catch(FileNotFoundException e){
            Log.e(TAG, "File not found", e);
            return ERROR;
        } catch (IOException e){
            Log.e(TAG, "IO exception", e);
            return ERROR;
        }

        return OK;
    }

    private byte[] intToByteArray(int a)
    {
        String temp = String.valueOf(a);
        return temp.getBytes();
    }

    private byte[] longToByteArray(long a)
    {
        String temp = String.valueOf(a);
        return temp.getBytes();
    }

    private int byteArrayToInt(byte[] a)
    {
        String temp = new String(a);
        return Integer.valueOf(temp);
    }

    private Long byteArrayToLong(byte[] a)
    {
        String temp = new String(a);
        return Long.valueOf(temp);
    }
}
