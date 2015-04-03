package hypd.battery;

import android.content.res.Resources;
import android.os.BatteryManager;

import com.google.gson.annotations.Expose;

public class BatteryPoint {
    // @Expose tells GSON to serialise these fields, ones not with this annotation will be ignored
    @Expose
    public String health, technology, plugged, status;
    @Expose
    public String propertyChargeCounter, propertyCurrentNow, propertyCurrentAverage, propertyEnergyCounter;
    @Expose
    public int level, scale, temperature, voltage;
    @Expose
    public boolean present;

    // Only used to get Strings from res/values/strings.xml
    // don't need to store it and increase size
    private Resources resources;

    public BatteryPoint() {
    }

    public BatteryPoint(Resources resources) {
        setPropertyChargeCounter(0);
        setPropertyCurrentAverage(0);
        setPropertyCurrentNow(0);
        setPropertyEnergyCounter(0);
        this.resources = resources;
    }

    public void setHealth(int healthCode) {
        switch (healthCode) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                health = resources.getString(R.string.battery_good);
                break;
            case BatteryManager.BATTERY_HEALTH_COLD:
                health = resources.getString(R.string.battery_cold);
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                health = resources.getString(R.string.battery_dead);
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                health = resources.getString(R.string.battery_over_voltage);
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                health = resources.getString(R.string.battery_over_voltage);
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                health = resources.getString(R.string.battery_unspecified_failure);
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            default:
                health = resources.getString(R.string.battery_unknown);
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPlugged(int pluggedCode) {
        if (pluggedCode == BatteryManager.BATTERY_PLUGGED_AC)
            this.plugged = resources.getString(R.string.battery_plugged_ac);
        else if (pluggedCode == BatteryManager.BATTERY_PLUGGED_USB)
            this.plugged = resources.getString(R.string.battery_plugged_usb);
        else if (pluggedCode == BatteryManager.BATTERY_PLUGGED_WIRELESS)
            this.plugged = resources.getString(R.string.battery_plugged_wireless);
        else this.plugged = null;
    }

    public void setStatus(int statusCode) {
        switch (statusCode) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                status = resources.getString(R.string.battery_status_charging);
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                status = resources.getString(R.string.battery_status_discharging);
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                status = resources.getString(R.string.battery_status_full);
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                status = resources.getString(R.string.battery_status_not_charging);
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
            default:
                status = resources.getString(R.string.battery_status_unknown);
        }
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public float getTemperatureInCelsius() {
        if (temperature < 100)
            return (float) temperature;
        else if (temperature < 1000)
            return temperature / 10;
        else
            return temperature / 100;
    }

    public float getTemperatureInKelvin() {
        return (float) (getTemperatureInCelsius() + 273.15);
    }

    public float getTemperatureInFahrenheit() {
        return (float) (getTemperatureInCelsius() * (9 / 5) + 32);
    }

    public void setPropertyChargeCounter(int propertyChargeCounter) {
        this.propertyChargeCounter = propertyChargeCounter +
                resources.getString(R.string.battery_property_charge_counter_unit);
    }

    public void setPropertyCurrentNow(int propertyCurrentNow) {
        this.propertyCurrentNow = propertyCurrentNow +
                resources.getString(R.string.battery_property_current_now_unit);
    }

    public void setPropertyCurrentAverage(int propertyCurrentAverage) {
        this.propertyCurrentAverage = propertyCurrentAverage +
                resources.getString(R.string.battery_property_current_average_unit);
    }

    public void setPropertyEnergyCounter(long propertyEnergyCounter) {
        this.propertyEnergyCounter = propertyEnergyCounter +
                resources.getString(R.string.battery_property_energy_counter_unit);
    }

    // Just for when you need to just print it to check, call BatteryPoint.toString()
    @Override
    public String toString() {
        return "{technology\":" + technology + "health\":" + health + "status\":" + status + "plugged\":" + plugged + "present\":" + present + "level\":" + level + "scale\":" + scale + "temperature\":" + temperature + "voltage\":" + voltage + "}";
    }
}