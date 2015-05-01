package hypd.battery;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class BatteryMonitoringService extends Service {

    private BroadcastReceiver mBatInfoReceiver;
    private ArrayList<BatteryPoint> batteryPoints;
    private SharedPreferences prefs;
    private Gson gson;
    private BatteryManager batteryManager;
    private BatteryPoint point;

    // Remember how this is the first one to be called when activity is created? same in Service
    @Override
    public void onCreate() {
        super.onCreate();
        // a null arraylist is a sad arraylist
        // most common source of error, not initialising them
        batteryPoints = new ArrayList<>();
        // Gets everything from res/

        /**
         * GSON is a library by Google that makes serialising data a breeze.
         * We need this because BatteryPoint has fields we don't wanna serialize.
         */
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        // gets preferences, it is a file that resides in our app's own private folder
        // use it to share stuff like user preferences, simple(and small) data etc
        prefs = getSharedPreferences("prefs", 0);

        // Not available before lollipop
        if (Build.VERSION.SDK_INT >= 21) {
            batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        }
        /**
         * You can safely NOT understand this, I don't either.
         * Remember broadcast receivers? yeah.. this is that..
         * BUT! for BATTERY_CHANGED(ie. some property of battery, level, temp etc changed)
         * you can not declare a broadcast receiver in your manifest like we can for everything else.
         * It makes sense since the app will be pulled to foreground too often.
         * So we gotta make a service and monitor it, if you don't need logging then we can safely
         * kill this service(and clean prefs) after we are done.
         */
        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                point = new BatteryPoint();
                if (bundle.containsKey(BatteryManager.EXTRA_HEALTH))
                    point.setHealth(bundle.getInt(BatteryManager.EXTRA_HEALTH));
                if (bundle.containsKey(BatteryManager.EXTRA_LEVEL))
                    point.setLevel(bundle.getInt(BatteryManager.EXTRA_LEVEL));
                if (bundle.containsKey(BatteryManager.EXTRA_PLUGGED))
                    point.setPlugged(bundle.getInt(BatteryManager.EXTRA_PLUGGED));
                if (bundle.containsKey(BatteryManager.EXTRA_PRESENT))
                    point.setPresent((bundle.getBoolean(BatteryManager.EXTRA_PRESENT)));
                if (bundle.containsKey(BatteryManager.EXTRA_SCALE))
                    point.setScale(bundle.getInt(BatteryManager.EXTRA_SCALE));
                if (bundle.containsKey(BatteryManager.EXTRA_STATUS))
                    point.setStatus(bundle.getInt(BatteryManager.EXTRA_STATUS));
                if (bundle.containsKey(BatteryManager.EXTRA_TECHNOLOGY))
                    point.setTechnology(bundle.getString(BatteryManager.EXTRA_TECHNOLOGY));
                if (bundle.containsKey(BatteryManager.EXTRA_TEMPERATURE))
                    point.setTemperature(bundle.getInt(BatteryManager.EXTRA_TEMPERATURE));
                if (bundle.containsKey(BatteryManager.EXTRA_VOLTAGE))
                    point.setVoltage(bundle.getInt(BatteryManager.EXTRA_VOLTAGE));

                if (Build.VERSION.SDK_INT >= 21) {
                    point.setPropertyChargeCounter(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER));
                    point.setPropertyCurrentAverage(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE));
                    point.setPropertyCurrentNow(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW));
                    point.setPropertyEnergyCounter(batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER));
                }
                batteryPoints.add(point);
                System.out.println("working " + point.toString());
                saveData(point);
            }
        };
    }

    /**
     * Rewrites the serialised arraylist in prefs and also adds the latest point, if you need it alag se
     *
     * @param nuPoint BatteryPoint object to save
     */
    private void saveData(BatteryPoint nuPoint) {
        prefs.edit().putString("points", gson.toJson(batteryPoints))
                .putString("latest", gson.toJson(nuPoint))
                .apply();
    }

    // for when you don't have latest point to offer to the mighty sharedPreferences
    private void saveData() {
        prefs.edit().putString("points", gson.toJson(batteryPoints)).apply();
    }

    // This is unique to service, everytime something 'starts' a service, this is called.
    // If service isn't running already, onCreate will be called, if it is.. system will skip to here.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String values = prefs.getString("points", "");
        if (values != null && !values.isEmpty()) {
            ArrayList<BatteryPoint> points = new Gson().fromJson(
                    values,
                    new TypeToken<ArrayList<BatteryPoint>>() {
                    }.getType()
            );
            prefs.edit().clear().apply(); //clears prefs when app first opened to wipe old data, get rid of this
            batteryPoints.clear();
            batteryPoints.addAll(points);
        }

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return super.onStartCommand(intent, flags, startId);
    }

    // Don't be a hoarder, we don't need no too much logging
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        unregisterReceiver(mBatInfoReceiver);
        saveData();
        stopSelf();
    }

    // this is called right before service is about to be destroyed(opposite of create, as you might have guessed :P)
    // Point of no return, save shit and release resources.
    @Override
    public void onDestroy() {
        super.onDestroy();
        saveData();
        unregisterReceiver(mBatInfoReceiver);
    }

    // Is supposed to be a way for activity and service to communicate
    // But I honestly never have used it, the sharedpreferences listener works okay but this might be
    // more efficient
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}