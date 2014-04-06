package com.sb.elsinore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.json.simple.JSONObject;


/********************
 * This class is for storing the mash steps.
 * It automatically updating the process as it goes.
 *
 * @author Doug Edey
 *
 */

public class MashControl implements Runnable {

    /**
     * The output PID to be controlled & read from.
     */
    private String outputControl = "";

    /**
     * The Pump to be controlled (not in use yet).
     */
    private String pumpControl = "";

    /**
     * A flag to tell the thread to shutdown.
     */
    private boolean shutdownFlag = false;

    /**
     * The default variance.
     */
    private double varianceF = 2.0;

    /**
     * The list of mash steps, position -> Step.
     */
    private HashMap<Integer, MashStep> mashStepList =
            new HashMap<Integer, MashStep>();

    /**
     * Append a new mashstep to the list.
     * @return The new mash step
     */
    public final MashStep addMashStep() {
        return addMashStep(mashStepList.size());
    }

    /**
     * Add a mashstep at a position, overriding the old one.
     * @param position The position to add the mashstep at
     * @return The new Mash Step
     */
    public final MashStep addMashStep(final int position) {
        mashStepList.put(position, new MashStep());
        return mashStepList.get(position);
    }

    /**
     * Get the current size of the mash step list.
     * @return The size of the mash step list
     */
    public final int getMashStepSize() {
        return mashStepList.size();
    }

    /**
     * Get the mash step at the specified position.
     * @param position The position to get the mash step at
     * @return The mash step at the specified position.
     */
    public final MashStep getMashStep(final Integer position) {
        Iterator<Entry<Integer, MashStep>> mashIt =
                mashStepList.entrySet().iterator();
        Entry<Integer, MashStep> e;

        while (mashIt.hasNext()) {
            e = mashIt.next();
            if (e.getKey() == position) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * Get the current mash step that's activated.
     * @return And entry with the position and the mash step
     */
    public final Entry<Integer, MashStep> getCurrentMashStep() {
        Iterator<Entry<Integer, MashStep>> mashIt =
            mashStepList.entrySet().iterator();
        Entry<Integer, MashStep> e;

        while (mashIt.hasNext()) {
            e = mashIt.next();
            if (e.getValue().isActive()) {
                return e;
            }
        }
        return null;
    }

    /**
     * A loop to run for the thread that checks the states.
     */
    @Override
    public final void run() {
        // Run through and update the times based on the currently active step
        Entry <Integer, MashStep> mashEntry = getCurrentMashStep();

        MashStep currentStep = null;
        Integer currentStepPosition = -1;

        // active step
        if (mashEntry != null) {
            currentStep = mashEntry.getValue();
            currentStepPosition = mashEntry.getKey();
        }

        PID currentPID = LaunchControl.findPID(getOutputControl());

        while (true) {
            // Is there a step and an output control?
            if (currentStep != null && currentPID != null) {
                mashEntry = getCurrentMashStep();

                // active step
                if (mashEntry != null) {
                    currentStep = mashEntry.getValue();
                    currentStepPosition = mashEntry.getKey();
                    BrewServer.LOG.warning("Found an active mash step: "
                        + currentStepPosition);
                }
            }

            if (currentStep != null && currentPID != null) {
                // Do stuff with the active step
                Date cDate = new Date();

                // Does the times need to be changed?
                double currentTempF = currentPID.getTempProbe().getTempF();
                BrewServer.LOG.warning("Current Temp: " + currentTempF
                        + " Target: " + currentStep.getTargetTempAs("F"));

                // Give ourselves a 2F range, this can be changed in the future
                if (currentTempF <= currentStep.getUpperTargetTempAs("F")
                    && currentTempF >= currentStep.getLowerTargetTempAs("F")) {
                    BrewServer.LOG.warning("Target mash temp");

                    if (currentStep.getStart() == null) {
                        BrewServer.LOG.warning("Setting start date");
                        // We've hit the target step temp, set the start date
                        DateTime tDate = new DateTime();
                        currentStep.setStart(tDate.toDate());

                        // set the target End Date stamp
                        tDate = tDate.plusMinutes(currentStep.getDuration());
                        currentStep.setTargetEnd(tDate.toDate());
                    }
                }

                // Have we gone past this mashStep Duration?
                if (currentStep.getTargetEnd() != null
                    && cDate.compareTo(currentStep.getTargetEnd()) >= 0) {
                    BrewServer.LOG.warning("End Temp hit");
                    // Over or equal to the target end time, deactivate
                    currentStep.deactivate(true);
                    // Get the next step target time
                    currentStepPosition += 1;
                    currentStep = getMashStep(currentStepPosition);
                    currentStep.activate();

                    currentPID.setTemp(
                        currentStep.getTargetTempAs(
                            currentPID.getTempProbe().getScale()));
                }

                // Does the target temp need to be updated
                if (currentPID.getTempF() != currentStep.getTargetTempAs("F")) {
                    String tScale = currentPID.getTempProbe().getScale();
                    currentPID.setTemp(currentStep.getTargetTempAs(tScale));
                }
            }

            try {
                // Sleep for 10 seconds
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // We got woken up.
                if (isShutdownFlag()) {
                    return;
                }
            }
        }
    }

    /**
     * Activate the step at the selected position.
     * Deactivates the other mash steps.
     * @param position The position to activate the step at
     * @return True if activated OK, false if there's an error
     */
    public final boolean activateStep(final Integer position) {
        // deactivate all the steps first
        MashStep mashEntry = getMashStep(position);

        // Do we have a value
        if (mashEntry == null) {
            BrewServer.LOG.warning("Index out of bounds");
            return false;
        }

        // Now we can reset the others
        if (!deactivateStep(-1)) {
            BrewServer.LOG.warning("Couldn't disable all the mash steps");
            return false;
        }

        mashEntry.activate();
        return true;
    }

    /**
     * Deactivate the step at the position specified.
     * @param position The step to deactivate. If < 0 deactivate all.
     * @return True if deactivated OK, false if not.
     */
    public final boolean deactivateStep(final Integer position) {
        // deactivate all the steps first

        if (position >= 0) {
            MashStep mashEntry = getMashStep(position);

            // Do we have a value
            if (mashEntry == null) {
                BrewServer.LOG.warning("Index out of bounds");
                return false;
            }
            mashEntry.deactivate();
        } else {
            // Otherwise deacctivate all the steps
            for (Entry<Integer, MashStep> mEntry : mashStepList.entrySet()) {
                mEntry.getValue().deactivate(false);
            }
        }

        return true;
    }

    /**
     * Return the current state of this MashControl as a JSON string.
     * @return The String representing the current state.
     */
    public final String getJSONDataString() {
        return getJSONData().toJSONString();
    }

    /**
     * Get the current status as a JSONObject.
     * @return The JSONObject representing the current state.
     */
    @SuppressWarnings("unchecked")
    public final JSONObject getJSONData() {
        JSONObject masterObject = new JSONObject();
        DateFormat lFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        masterObject.put("pid", this.getOutputControl());

        for (Entry<Integer, MashStep> e : mashStepList.entrySet()) {
            MashStep step = e.getValue();
            JSONObject mashstep = new JSONObject();
            mashstep.put("target_temp", step.getTargetTemp());
            mashstep.put("target_temp_unit", step.getTempUnit());
            mashstep.put("duration", step.getDuration());
            mashstep.put("method", step.getMethod());
            mashstep.put("type", step.getType());

            if (step.isActive()) {
                mashstep.put("active", true);
            }

            try {
                mashstep.put("start_time", lFormat.format(step.getStart()));
                mashstep.put("target_time",
                    lFormat.format(step.getTargetEnd()));
                mashstep.put("end_time", lFormat.format(step.getEnd()));
            } catch (NullPointerException npe) {
                // We know that some of these may be null, but don't care
                BrewServer.LOG.info("Failed to get a date: "
                    + npe.getLocalizedMessage());
            }

            masterObject.put(e.getKey(), mashstep);
        }

        return masterObject;
    }

    /**
     * @return Is the shutdown flag set?
     */
    public final boolean isShutdownFlag() {
        return shutdownFlag;
    }

    /**
     * @param newFlag Value to set the shutdown flag to
     */
    public final void setShutdownFlag(final boolean newFlag) {
        this.shutdownFlag = newFlag;
    }

    /**
     * @return the outputControl
     */
    public final String getOutputControl() {
        return outputControl;
    }

    /**
     * @param newControl the outputControl to set
     */
    public final void setOutputControl(final String newControl) {
        this.outputControl = newControl;
    }
}
