/*******************************************************************************
 * ServiceHandler.java.java, Created: Apr 27, 2012
 *
 * Part of Muni Project
 *
 * Copyright (c) 2012 : NDS Limited
 *
 * P R O P R I E T A R Y &amp; C O N F I D E N T I A L
 *
 * The copyright of this code and related documentation together with any
 * other associated intellectual property rights are vested in NDS Limited
 * and may not be used except in accordance with the terms of the licence
 * that you have entered into with NDS Limited. Use of this material without
 * an express licence from NDS Limited shall be an infringement of copyright
 * and any other intellectual property rights that may be incorporated with
 * this material.
 *
 * ******************************************************************************
 * ******     Please Check GIT for revision/modification history    *******
 * ******************************************************************************
 */

package com.safecell;

import com.safecell.networking.ConfigurationHandler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * @author uttama
 */
public class ServiceHandler {

    private static ServiceHandler mServiceHandler;

    private final String TAG = ConfigurationHandler.class.getSimpleName();
    
    private Context mContext;
    
    private ServiceConnection mConnection;
    
    /** The Constant NOT_BINDED. */
    public static final int NOT_BINDED = 0;
    /** The Constant BINDING_IN_PROGRESS. */
    public static final int BINDING_IN_PROGRESS = 1;
    /** The Constant BINDING_SUCESS. */
    public static final int BINDING_SUCESS = 2;
    
    /** The binding status. */
    private int bindingStatus;

    
    public static ServiceHandler getInstance(Context context) {
        if (mServiceHandler == null) {
            mServiceHandler = new ServiceHandler(context);
        }
        return mServiceHandler;
    }


    /** private constructor */
    private ServiceHandler(Context context) {
        this.mContext = context;
        initService();
    }

    /**
     * 
     */
    private void initService() {
        setBindingStatus(NOT_BINDED);
        createConnection();
        
    }
    
    private void createConnection(){
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "Service disconnected");
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                setBindingStatus(NOT_BINDED);


            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "Service Connected");
                setBindingStatus(BINDING_SUCESS);

            }

        };
    }


    /**
     * Bind service.
     */
    public synchronized void bindService() {
        Log.d(TAG, "BIND SERVICE");
        switch (getBindingStatus()) {
        case NOT_BINDED:
            Intent serviceIntent = new Intent(mContext,
                    TrackingService.class);
            mContext.bindService(serviceIntent, mConnection,
                    Context.BIND_AUTO_CREATE);
            setBindingStatus(BINDING_IN_PROGRESS);
            break;
        case BINDING_IN_PROGRESS:
            try {
                wait();
            } catch (InterruptedException e) {
                Log.d(TAG,
                        "InterruptedException : ServiceInfo = "
                                + e.getMessage());
            }
        case BINDING_SUCESS:
            break;
        default:
            break;
        }
    }

   
    /**
     * Sets the binding status.
     * 
     * @param bindingStatus
     *            the new binding status
     */
    private synchronized void setBindingStatus(int bindingStatus) {
        this.bindingStatus = bindingStatus;
    }
    
    /**
     * Gets the binding status.
     * 
     * @return the binding status
     */
    public int getBindingStatus() {
        return bindingStatus;
    }
    
    /**
     * Unbind the service and remove the registered callback.
     */
    public void unBind() {
        if (mConnection == null) {
            throw new IllegalStateException("mConnection cannot be null");
        }
        mContext.unbindService(mConnection);
        setBindingStatus(NOT_BINDED);
    }




}
