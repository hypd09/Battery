package hypd.battery.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import hypd.battery.BatteryPoint;
import hypd.battery.MainActivity;
import hypd.battery.R;

public class ChartFragment extends Fragment {
    public static final String TYPE_LEVEL = "level";
    public static final String TYPE_VOLTAGE = "volt";
    public static final String TYPE_TEMP = "temp";
    private static final String ARG_PARAM1 = "param1";
    private final int maxDataPoints = 300;
    Handler timeHandler = new Handler();
    private double date = 0;
    private String type;
    private long startTime;
    private LineGraphSeries<DataPoint> pointSeries;
    Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                BatteryPoint point = ((MainActivity) getActivity()).latestPoint;
                if (point != null)
                    updateChart(point);
                timeHandler.postDelayed(this, 1000);
            }
        }
    };
    private GraphView graph;

    public ChartFragment() {
        // Required empty public constructor
    }

    public static ChartFragment newInstance(String type) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chart, container, false);
        graph = (GraphView) rootView.findViewById(R.id.fragment_graph_view);
        pointSeries = new LineGraphSeries<DataPoint>();
        graph.addSeries(pointSeries);
        return rootView;
    }

    public void startMonitoring() {
        timeHandler.post(timeRunnable);
    }

    public void stopMonitoring() {
        timeHandler.removeCallbacks(timeRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis() / 1000;
    }

    public void updateChart(BatteryPoint point) {
        date++;
        switch (type) {
            case TYPE_LEVEL:
                pointSeries.appendData(new DataPoint(date, (double) point.level), true, maxDataPoints);
                break;
            case TYPE_TEMP:
                if (MainActivity.UnitLocale.getDefault().equals(MainActivity.UnitLocale.Imperial))
                    pointSeries.appendData(new DataPoint(date, (double) point.getTemperatureInFahrenheit()), true, maxDataPoints);
                else
                    pointSeries.appendData(new DataPoint(date, (double) point.getTemperatureInCelsius()), true, maxDataPoints);
                break;
            case TYPE_VOLTAGE:
                pointSeries.appendData(new DataPoint(date, point.getVoltage()), true, maxDataPoints);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timeRunnable);
    }
}
