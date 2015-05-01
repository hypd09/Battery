package hypd.battery;

import android.os.BatteryManager;

import com.google.gson.annotations.Expose;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class BatteryPoint {
    public static String battery_cold = "Cold",
            battery_dead = "Dead",
            battery_good = "Good",
            battery_overheat = "Overheat",
            battery_over_voltage = "Over Voltage",
            battery_unknown = "Unknown",
            battery_unspecified_failure = "Unknown failure",
            battery_plugged_ac = "Plugged in (AC)",
            battery_plugged_usb = "Plugged in (USB)",
            battery_plugged_wireless = "Plugged in (Wireless)",
            battery_status_charging = "Plugged in",
            battery_status_discharging = "Discharging",
            battery_status_full = "Full",
            battery_status_not_charging = "Not charging",
            battery_status_unknown = "Unknown";
    @Expose
    public String health, technology, plugged, status;
    @Expose
    public String propertyChargeCounter, propertyCurrentNow, propertyCurrentAverage, propertyEnergyCounter;
    @Expose
    public int level, scale, temperature, voltage;
    @Expose
    public boolean present, isCharging;
    DecimalFormat df = new DecimalFormat("##.##");
    // @Expose tells GSON to serialise these fields, ones not with this annotation will be ignored
    private String micro_unit = "\u00b5";
//    private String battery_property_capacity = "Capacity";
//    private String battery_property_charge_counter = "Charge";
//    private String battery_property_current_average = "Current(average)";
//    private String battery_property_current_now = "Current(now)";
//    private String battery_property_energy_counter = "Energy";

    public BatteryPoint() {
        setPropertyChargeCounter(0);
        setPropertyCurrentAverage(0);
        setPropertyCurrentNow(0);
        setPropertyEnergyCounter(0);
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    public void setHealth(int healthCode) {
        switch (healthCode) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                health = battery_good;
                break;
            case BatteryManager.BATTERY_HEALTH_COLD:
                health = battery_cold;
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                health = battery_dead;
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                health = battery_over_voltage;
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                health = battery_overheat;
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                health = battery_unspecified_failure;
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            default:
                health = battery_unknown;
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPlugged(int pluggedCode) {
        if (pluggedCode == BatteryManager.BATTERY_PLUGGED_AC) {
            this.plugged = battery_plugged_ac;
            isCharging = true;
        } else if (pluggedCode == BatteryManager.BATTERY_PLUGGED_USB) {
            this.plugged = battery_plugged_usb;
            isCharging = true;
        } else if (pluggedCode == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
            this.plugged = battery_plugged_wireless;
            isCharging = false;
        } else {
            this.plugged = null;
            isCharging = false;
        }
    }

    public void setStatus(int statusCode) {
        switch (statusCode) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                status = battery_status_charging;
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                status = battery_status_discharging;
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                status = battery_status_full;
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                status = battery_status_not_charging;
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
            default:
                status = battery_status_unknown;
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

    public float getVoltage() {
        float v = voltage / 1000f;
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        return Float.parseFloat(df.format(v));
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public float getTemperatureInCelsius() {
        if (temperature < 100f)
            return Float.parseFloat(df.format(temperature));
        else if (temperature < 1000f)
            return Float.parseFloat(df.format(temperature / 10f));
        else
            return Float.parseFloat(df.format(temperature / 100f));
    }

    public float getTemperatureInKelvin() {
        return (getTemperatureInCelsius() + 273.15f);
    }

    public float getTemperatureInFahrenheit() {
        float tempInC = 0;
        if (temperature < 100f)
            tempInC = (float) temperature;
        else if (temperature < 1000f)
            tempInC = temperature / 10f;
        else
            tempInC = temperature / 100f;

        return Float.parseFloat(df.format((tempInC * (9f / 5f) + 32f)));
    }

    public void setPropertyChargeCounter(int propertyChargeCounter) {
        this.propertyChargeCounter = propertyChargeCounter + micro_unit + "Ah";
    }

    public void setPropertyCurrentNow(int propertyCurrentNow) {
        this.propertyCurrentNow = propertyCurrentNow + micro_unit + "A";
    }

    public void setPropertyCurrentAverage(int propertyCurrentAverage) {
        this.propertyCurrentAverage = propertyCurrentAverage + micro_unit + "A";
    }

    public void setPropertyEnergyCounter(long propertyEnergyCounter) {
        this.propertyEnergyCounter = propertyEnergyCounter + "nWh";
    }

    // Just for when you need to just print it to check, call BatteryPoint.toString()
    @Override
    public String toString() {
        return "{technology\":" + technology + "health\":" + health + "status\":" + status + "plugged\":" + plugged + "present\":" + present + "level\":" + level + "scale\":" + scale + "temperature\":" + temperature + "voltage\":" + voltage + "}";
    }
}