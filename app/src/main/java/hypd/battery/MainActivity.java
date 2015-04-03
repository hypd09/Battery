package hypd.battery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lecho.lib.hellocharts.view.LineChartView;


public class MainActivity extends ActionBarActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences prefs;

    @InjectView(R.id.battery_chart)
    LineChartView chartView;
    @InjectView(R.id.data_list_view)
    ListView dataListView;
    @InjectView(R.id.battery_percent_text)
    TextView currentBatt;

    private ArrayList<BatteryPoint> points;
    private ArrayList<Integer> percentList;
    private BatteryPoint point;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * Butterknife is a very cool library that makes work of assigning views from xml to java
         * very easy
         */
        ButterKnife.inject(this);

        prefs = getSharedPreferences("prefs", 0);
        points = new ArrayList<>();
        /**
         * GSON is a library by Google that makes serialising data a breeze.
         * We need this because BatteryPoint has fields we don't wanna serialize.
         */
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String values = prefs.getString("points", "");
        if (values != null && !values.isEmpty()) {
            ArrayList<BatteryPoint> points = new Gson().fromJson(
                    values,
                    new TypeToken<ArrayList<BatteryPoint>>() {
                    }.getType()
            );
            // clear the present list and add all updated
            // can be done more efficiently, but I am lazy
            points.clear();
            points.addAll(points);
        }
        // This will monitor our sharedPreferences, which we edit in the service
        // Sending data from service to Activity and managing state is difficult
        // This is the easiest solution
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Gets called when service adds a new point to the serialised field 'points'
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("points")) {
            String values = prefs.getString("points", "");
            if (values != null && !values.isEmpty()) {
                ArrayList<BatteryPoint> points = gson.fromJson(
                        values,
                        new TypeToken<ArrayList<BatteryPoint>>() {
                        }.getType()
                );
                points.clear();
                points.addAll(points);
                ChangeValues(points.get(points.size() - 1));
            }
        }
    }

    /**
     * We got new data, use it to populate your screen
     *
     * @param point latest object, to display percentage on main screen
     */
    private void ChangeValues(BatteryPoint point) {
        this.point = point;
        currentBatt.setText(point.level + "%");
        currentBatt.setText(point.level + "%");
        for (BatteryPoint p : points) {
            percentList.clear();
            percentList.add(p.level);
        }
        for (int i = points.size(), cnt = 2; i > 0 && cnt > 0; cnt--, i--) {
            //populate an array or something to use to display shit
            Log.d("yay?", "yay");
        }
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
}