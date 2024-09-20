package com.example.wgwsimcore;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;

import com.acmerobotics.roadrunner.Vector2d;
import com.example.trajectoryactions.SimConfig.SimRobot;
import com.example.trajectoryactions.SimConfig.Robots;
import com.google.gson.Gson;

//
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

// this class stores the core functions need to run the simulator, while providing abstraction
// from the GUI portion of the simulator
// GUI portion should extend this class and fill in the abstract classes need to interface
// with the gui and system resources.
// this allow easy use on a phone app, simulator, windows executable etc.
// exampe:  https://www.baeldung.com/java-template-method-pattern
public abstract  class WGWsimCore {

    private final double fieldSizeInches = 144.0;
    private final Robots robots = new Robots();
    //    private final ArrayList<TelemetryPacket> telemetryHistory = new ArrayList<>();
    private final long timerTickms = 100;
    private final double defaultActionDurationMs = 30.0 * 1000;
//    SimRobot needRobotList = robots.simRobots.get("MiniMe"); // todo  NEED a LIST of robots that are run actions!

    //    private Action action;
//    private boolean done = true;
    private boolean paused = false;
    private boolean startNewRun = false;
    private boolean newFieldOverlay = false;
    private int showRobotIndex = 0;
    private double actionDuration = defaultActionDurationMs;

    // These Abstract classes must be implemented in the instantiated clases
    public abstract void drawLineInches(Vector2d start, Vector2d end);

    public abstract void drawCircleInches(double xInches, double yInches, double rInches);

    public abstract void setColor(String color);

    public abstract void telemetryTextAddLine(String str);

    public abstract void telemetryTextClear();

    public double getFieldDimensionInches() {
        return fieldSizeInches;
    }

    // return a list of Robot names
//    public List<String> getRobotNames() {
//        List<String> retVal = new ArrayList<String>();
//
//        // add all the names of the paths.
//        retVal.addAll(robots.simRobots.keySet());
//
//        return retVal;
//    }

    public int getNumRobots() {
        return robots.simRobots.size();
    }

    public String getRobotName(int index){
        return robots.simRobots.get(index).getName();
    }

    public boolean getRobotEnabled(int index) {
        return robots.simRobots.get(index).getEnabled();
    }


    // return a list of the path names
    public List<String> getActionNames(int  robotIndex) {
        return new ArrayList<>(robots.simRobots.get(robotIndex).paths.keySet());
    }

    public void setCurrentAction(int robotIndex, String actionName) {
        robots.simRobots.get(robotIndex).setAction(actionName);
        actionDuration = defaultActionDurationMs;
    }

    public void setCurrentAction(String robotName, String actionName) {
        for (int i=0; i < robots.simRobots.size();i++) {  // have to look for the robot name.
            if (robots.simRobots.get(i).getName() == robotName) {
                setCurrentAction(i, actionName);
                return;
            }
        }
    }

    public void clearRobotActions() {
        for (int r = 0; r < robots.simRobots.size(); r++)
            robots.simRobots.get(r).setAction(null);
    }


    public void startAction() {
            startNewRun = true;
    }

    public void pauseAction() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    public boolean isPaused() {return paused;}

    public double getActionDuration() {return actionDuration;}

//    public boolean isDone() {return done;}

    private double simTimeMs = 0;
    private int runCount = 0;

    public double getsimTimeMs() {return simTimeMs;};
    public double getCurrentTimeMs() {return showRobotIndex*timerTickms;};

    public long getTimerTickms() {return timerTickms;}

    boolean simulationComplete = true;
    public void timerTick() {

        // first we run the simulator and collect the telemetry packets.
        // We store the telemetry packets for replay.  This could be a memory hog!
        if (startNewRun) {
            simTimeMs = 0;
            showRobotIndex = 0;
            simulationComplete = false;
            startNewRun = false;
            paused = false;
            runCount = 0;
//            needRobotList.clearTelemetryHistory();  // todo telemetryHistory.clear();
            for (int i=0; i< robots.simRobots.size(); i++) {   // all robots, clear telemetry
                SimRobot robot = robots.simRobots.get(i);
                robot.clearTelemetryHistory();
            }
            robots.startGameTimer();

        }

        // run the simmulator and store the packets
        if (!simulationComplete) {
            boolean needToRun = false;
            for (int i=0; i< robots.simRobots.size(); i++) {   // all robots, clear telemetry
                TelemetryPacket dashTelemetryPacket = new TelemetryPacket();
                SimRobot robot = robots.simRobots.get(i);
                needToRun |= robot.runAction(dashTelemetryPacket);
//                dashTelemetryPacket.put("Elapsed TIme", simTimeMs / 1000);  // add a time statement to each packet
                robot.putTelemetryHistory(dashTelemetryPacket);  // telemetryHistory.add(dashTelemetryPacket); // store the packet for later use
            }
            simulationComplete = !needToRun;
            if (simulationComplete){
                actionDuration = simTimeMs;
            } else {
                simTimeMs += timerTickms;
                runCount++;
            }
        }

        // now display the robot on screen.
        if ((showRobotIndex < runCount-1)) {
            //decodeTelemetry(telemetryHistory.get(showRobotIndex),  showRobotIndex*timerTickms);
            telemetryTextClear();
            TelemetryPacket timePacket = new TelemetryPacket();
            timePacket.put("Elapsed TIme", (double)(showRobotIndex * getTimerTickms()) / 1001);  // add a time statement to each packet
            decodeTelemetry(timePacket, "");
            for (int i=0; i< robots.simRobots.size(); i++) {   // all robots, clear telemetry
                SimRobot robot = robots.simRobots.get(i);
                decodeTelemetry(robot.getTelemetryHistory(showRobotIndex), robots.simRobots.get(i).getName());
//                telemetryTextAddLine("\n");
            }
            if (! paused) {
                showRobotIndex++;
            }
        }
    }

    public void showAtTimeMs(double timeMs) {
        int index = (int) (timeMs / timerTickms);
        int len = runCount;// telemetryHistory.size();
        if (index>len)
            index = len;
        showRobotIndex = index;
        paused = true;
    }

    public boolean isNewFieldOverlay() {return newFieldOverlay;}

    public void clearNewFieldOverlay() {newFieldOverlay = false;}

    private void decodeTelemetry(TelemetryPacket dashTelPacket, String robotName) {

        Gson gson = new Gson();
        String jsonString = gson.toJson(dashTelPacket);

        setColor("#FF10F0");  // set a default Color.rgb(255, 16, 240));
        boolean newTelemetryText = false;

        try {
            JSONObject obj = new JSONObject(jsonString);
            // parse the json data for text to display
            JSONObject data = obj.getJSONObject("data");
            JSONArray names = data.names();
            if (names != null) {
                //telemetryTextClear();
                for (int i = 0; i < names.length(); i++) {
                    String name = names.getString(i);
                    double value = data.getDouble(name);
                    String formatedStr = String.format("%s %s : %.2f\n", robotName, name, value);
                    telemetryTextAddLine(formatedStr);
                    newTelemetryText = true;
                }
            }

            if(newTelemetryText) {
                telemetryTextAddLine("\n");
            }


            // parse the json for graphics to display
            // not all graphics have been implemented.  if you use more from roadrunner
            // you will need to implement them yourself.
            JSONObject fieldOverlay = obj.getJSONObject("fieldOverlay");
            JSONArray opsJArray = fieldOverlay.getJSONArray("ops");
            for (int i = 0; i < opsJArray.length(); i++) {
                JSONObject op = opsJArray.getJSONObject(i);
                String type = op.getString("type");
                newFieldOverlay = true;
                switch (type) {
                    case "polyline":
                        JSONArray xJasonArray = op.getJSONArray("xPoints");
                        JSONArray yJasonArray = op.getJSONArray("yPoints");
                        double lastX = 0, lastY = 0;
                        Vector2d lastVector = new Vector2d(0,0);
                        for (int j = 0; j < xJasonArray.length(); j++) {
                            Vector2d vec = new Vector2d(xJasonArray.getDouble(j), yJasonArray.getDouble(j));
                            if (j != 0)
                                drawLineInches(lastVector, vec);
                            lastVector = vec;
                        }
                        break;
                    case "stroke":
                        String stringColor = op.getString("color"); //
//                        setColor(Integer.parseInt(stringColor));
                        setColor(stringColor);
                        break;
                    case "circle":
                        double x = op.getDouble("x");
                        double y = op.getDouble("y");
                        double radius = op.getDouble("radius");
                        drawCircleInches(x,y,radius);
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
