/*
 * Copyright (C) 2011 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asksven.android.common.privateapiproxies;


import java.lang.reflect.Field;

import java.lang.reflect.Method;
import java.util.Map;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Printer;
import android.util.SparseArray;
import com.asksven.android.common.privateapiproxies.BatteryStatsTypes;


/**
 * A proxy to the non-public API BatteryStats
 * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.3_r1/android/os/BatteryStats.java/?v=source
 * @author sven
 *
 */
public class BatteryStatsProxy
{
	/*
	 * Instance of the BatteryStatsImpl
	 */
	private Object m_Instance = null;
	@SuppressWarnings("rawtypes")
	private Class m_ClassDefinition = null;

    /**
	 * Default cctor
	 */
	public BatteryStatsProxy(Context context)
	{
		/*
		 * As BatteryStats is a service we need to get a binding using the IBatteryStats.Stub.getStatistics()
		 * method (using reflection).
		 * If we would be using a public API the code would look like:
		 * @see com.android.settings.fuelgauge.PowerUsageSummary.java 
		 * protected void onCreate(Bundle icicle) {
         *  super.onCreate(icicle);
		 *	
         *  mStats = (BatteryStatsImpl)getLastNonConfigurationInstance();
		 *
         *  addPreferencesFromResource(R.xml.power_usage_summary);
         *  mBatteryInfo = IBatteryStats.Stub.asInterface(
         *       ServiceManager.getService("batteryinfo"));
         *  mAppListGroup = (PreferenceGroup) findPreference("app_list");
         *  mPowerProfile = new PowerProfile(this);
    	 * }
		 *
		 * followed by
		 * private void load() {
         *	try {
         *   byte[] data = mBatteryInfo.getStatistics();
         *   Parcel parcel = Parcel.obtain();
         *   parcel.unmarshall(data, 0, data.length);
         *   parcel.setDataPosition(0);
         *   mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
         *           .createFromParcel(parcel);
         *   mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
         *  } catch (RemoteException e) {
         *   Log.e(TAG, "RemoteException:", e);
         *  }
         * }
		 */
		
		try
		{
	          ClassLoader cl = context.getClassLoader();
	          
	          m_ClassDefinition = cl.loadClass("com.android.internal.os.BatteryStatsImpl");
	          
	          // get the IBinder to the "batteryinfo" service
	          @SuppressWarnings("rawtypes")
			  Class serviceManagerClass = cl.loadClass("android.os.ServiceManager");
	          
	          // parameter types
	          @SuppressWarnings("rawtypes")
			  Class[] paramTypesGetService= new Class[1];
	          paramTypesGetService[0]= String.class;
	          
	          @SuppressWarnings("unchecked")
			  Method methodGetService = serviceManagerClass.getMethod("getService", paramTypesGetService);
	          
	          // parameters
	          Object[] paramsGetService= new Object[1];
	          paramsGetService[0] = "batteryinfo";
	     
	          IBinder serviceBinder = (IBinder) methodGetService.invoke(serviceManagerClass, paramsGetService); 

	          // now we have a binder. Let's us that on IBatteryStats.Stub.asInterface
	          // to get an IBatteryStats
	          // Note the $-syntax here as Stub is a nested class
	          @SuppressWarnings("rawtypes")
			  Class iBatteryStatsStub = cl.loadClass("com.android.internal.app.IBatteryStats$Stub");

	          //Parameters Types
	          @SuppressWarnings("rawtypes")
			  Class[] paramTypesAsInterface= new Class[1];
	          paramTypesAsInterface[0]= IBinder.class;

	          @SuppressWarnings("unchecked")
			  Method methodAsInterface = iBatteryStatsStub.getMethod("asInterface", paramTypesAsInterface);

	          // Parameters
	          Object[] paramsAsInterface= new Object[1];
	          paramsAsInterface[0] = serviceBinder;
	          	          
	          Object iBatteryStatsInstance = methodAsInterface.invoke(iBatteryStatsStub, paramsAsInterface);
	          
	          // and finally we call getStatistics from that IBatteryStats to obtain a Parcel
	          @SuppressWarnings("rawtypes")
			  Class iBatteryStats = cl.loadClass("com.android.internal.app.IBatteryStats");
	          
	          @SuppressWarnings("unchecked")
	          Method methodGetStatistics = iBatteryStats.getMethod("getStatistics");
	          byte[] data = (byte[]) methodGetStatistics.invoke(iBatteryStatsInstance);
	          
	          Parcel parcel = Parcel.obtain();
	          parcel.unmarshall(data, 0, data.length);
	          parcel.setDataPosition(0);
	          
	          @SuppressWarnings("rawtypes")
			  Class batteryStatsImpl = cl.loadClass("com.android.internal.os.BatteryStatsImpl");
	          Field creatorField = batteryStatsImpl.getField("CREATOR");
	          
	          // From here on we don't need reflection anymore
	          @SuppressWarnings("rawtypes")
			  Parcelable.Creator batteryStatsImpl_CREATOR = (Parcelable.Creator) creatorField.get(batteryStatsImpl); 
	          
	          m_Instance = batteryStatsImpl_CREATOR.createFromParcel(parcel);        
	    }
		catch( IllegalArgumentException e )
		{
	        m_Instance = null;
	    }
		catch (ClassNotFoundException e)
		{
	    	m_Instance = null;
	    }
		catch( Exception e )
		{
	    	m_Instance = null;
	    }    
	}
	
	/**
     * Returns the total, last, or current battery realtime in microseconds.
     *
     * @param curTime the current elapsed realtime in microseconds.
     * @param which one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long computeBatteryRealtime(long curTime, int which)
	{
    	Long ret = new Long(0);

        try
        {
          //Parameters Types
          @SuppressWarnings("rawtypes")
          Class[] paramTypes= new Class[2];
          paramTypes[0]= long.class;
          paramTypes[1]= int.class;          

          @SuppressWarnings("unchecked")
		  Method method = m_ClassDefinition.getMethod("computeBatteryRealtime", paramTypes);

          //Parameters
          Object[] params= new Object[2];
          params[0]= new Long(curTime);
          params[1]= new Integer(which);

          ret= (Long) method.invoke(m_Instance, params);

        }
        catch( IllegalArgumentException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            ret = new Long(0);
        }

        return ret;

	
	}
    
    /**
     * Return whether we are currently running on battery.
     */	
    
	public boolean getIsOnBattery(Context context)
	{
    	boolean ret = false;

        try
        { 
	    	@SuppressWarnings("unchecked")	
	    	Method method = m_ClassDefinition.getMethod("getIsOnBattery");

        	Boolean oRet = (Boolean) method.invoke(m_Instance);
        	ret = oRet.booleanValue();

        }
        catch( IllegalArgumentException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            ret = false;
        }    
        
        return ret;
	}
    
    /**
     * Returns the current battery percentage level if we are in a discharge cycle, otherwise
     * returns the level at the last plug event.
     */
    public int getDischargeCurrentLevel()
    {
    	int ret = 0;

        try
        {
        	@SuppressWarnings("unchecked")
        	Method method = m_ClassDefinition.getMethod("getDischargeCurrentLevel");

        	Integer oRet = (Integer) method.invoke(m_Instance);
        	ret = oRet.intValue();

        }
        catch( IllegalArgumentException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            ret = 0;
        }    
        
        return ret;
    }
	
    
	public SparseArray<? extends BatteryStatsTypes.Uid> getUidStats()
    {
    	SparseArray<? extends BatteryStatsTypes.Uid> uidStats = null;
    	// <? extends Uid> uidStats = mStats.getUidStats();
        try
        {
        	@SuppressWarnings("unchecked")
        	Method method = m_ClassDefinition.getMethod("getUidStats");
        	
        	uidStats = (SparseArray<? extends BatteryStatsTypes.Uid>) method.invoke(m_Instance);
        	final int which = BatteryStatsTypes.STATS_SINCE_CHARGED;
        	long uSecTime = this.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, which);
        	
        	int NU = uidStats.size();
            for (int iu = 0; iu < NU; iu++)
            {
            	BatteryStatsTypes.Uid u = uidStats.valueAt(iu);
                long wakelockTime = 0;
                Map<String, ? extends BatteryStatsTypes.Uid.Wakelock> wakelockStats = u.getWakelockStats();
                for (Map.Entry<String, ? extends BatteryStatsTypes.Uid.Wakelock> wakelockEntry
                        : wakelockStats.entrySet()) {
                	BatteryStatsTypes.Uid.Wakelock wakelock = wakelockEntry.getValue();
                    // Only care about partial wake locks since full wake locks
                    // are canceled when the user turns the screen off.
                	BatteryStatsTypes.Timer timer = wakelock.getWakeTime(BatteryStatsTypes.WAKE_TYPE_PARTIAL);
                    if (timer != null) {
                        wakelockTime += timer.getTotalTimeLocked(uSecTime, which);
                    }
                }

            }    
        	
        }
        catch( IllegalArgumentException e )
        {
            throw e;
        }
        catch( Exception e )
        {
        	uidStats = null;
        }    
        
        return uidStats;
    }

}