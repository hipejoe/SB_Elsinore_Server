package com.sb.elsinore;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Observable;

import static com.sb.elsinore.UrlEndpoints.*;

/**
 * Hosts the setting specific to a PID profile
 * Created by Douglas on 2016-05-15.
 */
public class PIDSettings extends Observable {

    public static final String DELAY = "delay";

    public String getGPIO() {
        return this.gpio;
    }

    public void setGPIO(String newGPIO) {
        this.gpio = newGPIO;
    }

    public BigDecimal getCycleTime() {
        return this.cycle_time;
    }

    public void setCycleTime(BigDecimal cycle_time) {
        this.cycle_time = cycle_time;
        setChanged();
        notifyObservers("cycle_time");
    }

    public BigDecimal getProportional() {
        return this.proportional;
    }

    public void setProportional(BigDecimal proportional) {
        this.proportional = proportional;
        setChanged();
        notifyObservers("proportional");
    }

    public BigDecimal getIntegral() {
        return this.integral;
    }

    public void setIntegral(BigDecimal integral) {
        this.integral = integral;
        setChanged();
        notifyObservers("integral");
    }

    public BigDecimal getDerivative() {
        return this.derivative;
    }

    public void setDerivative(BigDecimal derivative) {
        this.derivative = derivative;
        setChanged();
        notifyObservers("derivative");
    }

    public BigDecimal getDelay() {
        return this.delay;
    }

    public void setDelay(BigDecimal delay) {
        this.delay = delay;
        setChanged();
        notifyObservers(DELAY);
    }

    public PID_MODE getMode() {
        return this.mode;
    }

    public void setMode(PID_MODE mode) {
        this.mode = mode;
        setChanged();
        notifyObservers("mode");
    }

    public String getProfile() {
        return this.profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
        setChanged();
        notifyObservers("profile");
    }

    public boolean isInverted() {
        return this.inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        setChanged();
        notifyObservers("inverted");
    }

    /**
     * Hosts the mode of these settings (Heat or COOL for now)
     */
    public enum PID_MODE {
        HEAT,
        COOL,
    }

    /**
     * values to hold the settings.
     */
    @SerializedName(CYCLETIME)
    private BigDecimal cycle_time = new BigDecimal(0);
    @SerializedName(P)
    private BigDecimal proportional = new BigDecimal(0);
    @SerializedName(I)
    private BigDecimal integral = new BigDecimal(0);
    @SerializedName(D)
    private BigDecimal derivative = new BigDecimal(0);
    @SerializedName(DELAY)
    private BigDecimal delay = new BigDecimal(0);
    @SerializedName(MODE)
    private PID_MODE mode = PID_MODE.HEAT;
    @SerializedName(PROFILE)
    private String profile = "";
    @SerializedName(GPIO)
    private String gpio = null;
    @SerializedName(INVERTED)
    private boolean inverted = false;

    /**
     * Default constructor.
     */
    PIDSettings() {
        this.cycle_time = this.proportional =
                this.integral = this.derivative = new BigDecimal(0.0);
    }

}
