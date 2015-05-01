package hypd.battery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public BatteryPoint latestPoint;
    @InjectView(R.id.battery_percent_text)
    TextView batteryPercent;
    @InjectView(R.id.battery_voltage)
    TextView batteryVoltage;
    @InjectView(R.id.battery_health)
    TextView batteryHealth;
    @InjectView(R.id.battery_temp)
    TextView batteryTemp;
    @InjectView(R.id.battery_status)
    TextView batteryStatus;
    @InjectView(R.id.battery_tech)
    TextView batteryTech;
    @InjectView(R.id.charging_time)
    TextView chargingTime;
    //    @InjectView(R.id.charts)
//    ViewPager chartsView;
    @InjectView(R.id.fab_service)
    FloatingActionButton button;
    private SharedPreferences prefs;
    private ArrayList<BatteryPoint> points;
    private long initTime = -1;
    private Gson gson;
    private boolean isServiceRunning = false, isCharging = false;
    private Intent serviceIntent;
    private int startNormal, startPressed, stopNormal, stopPressed;

    private android.os.Handler timeHandler = new android.os.Handler();
    Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            // do your stuff - don't create a new runnable here!
            if (chargingTime != null && initTime != -1) {
                timeHandler.postDelayed(this, 1000);
                chargingTime.setText(DateUtils.getRelativeTimeSpanString(
                        initTime,
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS
                ));
            }
        }
    };
//    private ChartFragment levelFragment, tempFragment, voltageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /**
         * Butterknife is a very cool library that makes work of assigning views from xml to java
         * very easy
         */
        ButterKnife.inject(this);

        prefs = getSharedPreferences("prefs", 0);
        if (prefs.contains("is_service_running")) {
            isServiceRunning = prefs.getBoolean("is_service_running", false);
        }
        points = new ArrayList<>();

//        levelFragment = ChartFragment.newInstance(ChartFragment.TYPE_LEVEL);
//        voltageFragment = ChartFragment.newInstance(ChartFragment.TYPE_VOLTAGE);
//        tempFragment = ChartFragment.newInstance(ChartFragment.TYPE_TEMP);
//
//        ChartsAdapter adapter = new ChartsAdapter(getSupportFragmentManager());
//        chartsView.setAdapter(adapter);
//        chartsView.setOffscreenPageLimit(3);


        serviceIntent = new Intent(this, BatteryMonitoringService.class);
        /**
         * GSON is a library by Google that makes serialising data a breeze.
         * We need this because BatteryPoint has fields we don't wanna serialize.
         */
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String values = prefs.getString("points", "");
        if (values != null && !values.isEmpty()) {
            ArrayList<BatteryPoint> pointList = new Gson().fromJson(
                    values,
                    new TypeToken<ArrayList<BatteryPoint>>() {
                    }.getType()
            );
            // clear the present list and add all updated
            // can be done more efficiently, but I am lazy
            points.clear();
            points.addAll(pointList);
            if (isServiceRunning && points.size() > 0) {
                ChangeValues(points.get(points.size() - 1));
            }
        }

        button.setLabelVisibility(View.VISIBLE);
        button.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        button.setElevationCompat(4);
        button.setElevation(4);

        Resources resources = getResources();
        startNormal = resources.getColor(R.color.start_normal);
        startPressed = resources.getColor(R.color.start_pressed);
        stopNormal = resources.getColor(R.color.stop_normal);
        stopPressed = resources.getColor(R.color.stop_pressed);

        if (isServiceRunning) {
            button.setColorNormal(stopNormal);
            button.setColorPressed(stopPressed);
            button.setLabelText("Stop logging");
//            levelFragment.startMonitoring();
//            voltageFragment.startMonitoring();
//            tempFragment.startMonitoring();
        } else {
            button.setColorNormal(startNormal);
            button.setColorPressed(startPressed);
            button.setLabelText("Start logging");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isServiceRunning) {
                    button.setColorNormal(stopNormal);
                    button.setColorPressed(stopPressed);
                    button.setLabelText("Stop logging");
                    isServiceRunning = true;
                    startService(serviceIntent);
//                    levelFragment.startMonitoring();
//                    voltageFragment.startMonitoring();
//                    tempFragment.startMonitoring();
                } else {
                    button.setColorNormal(startNormal);
                    button.setColorPressed(startPressed);
                    button.setLabelText("Start logging");
                    isServiceRunning = false;
//                    levelFragment.stopMonitoring();
//                    voltageFragment.stopMonitoring();
//                    tempFragment.stopMonitoring();
                    stopService(serviceIntent);
                    chargingTime.setVisibility(View.GONE);
                    timeHandler.removeCallbacks(timeRunnable);
                }
                prefs.edit().putBoolean("is_service_running", isServiceRunning).apply();
            }
        });
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("points")) {
            String values = prefs.getString("points", "");
            if (values != null && !values.isEmpty()) {
                ArrayList<BatteryPoint> pointList = gson.fromJson(
                        values,
                        new TypeToken<ArrayList<BatteryPoint>>() {
                        }.getType()
                );
                points.clear();
                points.addAll(pointList);
                if (points.size() > 0)
                    ChangeValues(points.get(points.size() - 1));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        levelFragment.stopMonitoring();
//        voltageFragment.stopMonitoring();
//        tempFragment.stopMonitoring();
        timeHandler.removeCallbacks(timeRunnable);
    }

    /**
     * We got new data, use it to populate your screen
     *
     * @param point latest object, to display percentage on main screen
     */
    private void ChangeValues(BatteryPoint point) {
        batteryPercent.setText("Batt: " + point.level + "%");
        batteryVoltage.setText("Volt: " + Float.toString(point.getVoltage()) + " v");
        batteryHealth.setText("Health: " + point.health);
        batteryStatus.setText(point.status);
        batteryTech.setText("Tech: " + point.technology);
        batteryTemp.setText("Temp: " + (
                (UnitLocale.getDefault() == UnitLocale.Imperial) ?
                        Float.toString(point.getTemperatureInFahrenheit()) + "\u2103" :
                        Float.toString(point.getTemperatureInCelsius()) + "\u2109"
        ));
        latestPoint = point;
//        levelFragment.updateChart(point);
//        voltageFragment.updateChart(point);
//        tempFragment.updateChart(point);
        chargingStuff(point.isCharging);
    }

    private void chargingStuff(boolean nuCharging) {
        if (!isCharging && nuCharging) { //wasn't charging, now is
            isCharging = true;
            initTime = System.currentTimeMillis();
            prefs.edit().putLong("initTime", initTime).apply();
            chargingTime.setVisibility(View.VISIBLE);
            timeHandler.post(timeRunnable);
        } else if (isCharging && !nuCharging) {
            initTime = -1;
            prefs.edit().remove("initTime").apply();
            chargingTime.setVisibility(View.GONE);
            timeHandler.removeCallbacks(timeRunnable);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isCharging", isCharging);
        outState.putBoolean("isServiceRunning", isServiceRunning);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isCharging = savedInstanceState.getBoolean("isCharging");
        isServiceRunning = savedInstanceState.getBoolean("isServiceRunning");
    }

    /**
     * Use this to get if that person uses celsius or fahrenheit
     * then call BatteryPoint.getTemperatureInCelsius() or other.
     */
    public static class UnitLocale {
        public static UnitLocale Imperial = new UnitLocale();
        public static UnitLocale Metric = new UnitLocale();

        public static UnitLocale getDefault() {
            return getFrom(Locale.getDefault());
        }

        public static UnitLocale getFrom(Locale locale) {
            String countryCode = locale.getCountry();
            if ("US".equals(countryCode)) return Imperial; // USA
            if ("LR".equals(countryCode)) return Imperial; // liberia
            if ("MM".equals(countryCode)) return Imperial; // burma
            return Metric;
        }
    }
    /*
    private class ChartsAdapter extends FragmentPagerAdapter {
        String titles[] = {"Batt. %", "Voltage", "Temperature"};

        public ChartsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public float getPageWidth(int position) {
            return 0.8f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return levelFragment;
                case 1:
                    return voltageFragment;
                case 2:
                    return tempFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return titles.length;
        }
    }
    */
}