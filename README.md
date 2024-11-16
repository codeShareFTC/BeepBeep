# **BeepBeep \- FTC RoadRunner Simulator**

## Introduction

BeepBeep is a simulator for First Tech Challenge teams that are utilizing android studio and RoadRunner tool chains.  It is intended to aid in quick path development.  It is NOT a physics simulator,

## Motivation

In previous seasons we have developed roadrunner paths using other simulators.  All of these required  porting or modifying our code from the simulator into the robot.   We would end up with differences in these versions that made it difficult to keep the simulation and the physical robot synchronized.    
The primary goal of BeepBeep is to modularize the trajectory code so that the exact same code is run on the simulator or the robot.  No longer do we need to maintain two versions of our paths\!

## Acknowledgements

We want to thank all of the others that have worked to provide tools for FTC teams.  Specifically Road Runner.  Additionally we learned a lot from using and then reviewing Noah Bres’s MeepMeep.  In fact, the name BeepBeep was picked as a nod of respect to MeepMeep\!

## Architecture

We strongly believe users of BeepBeep should modify it and make it fit their needs.  To encourage this we are sharing BeepBeep as source project rather than a library.  Please have fun with it. If you make it do something cool let us know\!    BeepBeep is installed into your project as a submodule.

### BeepBeepCore

this is the low level simulator code that interfaces with your trajectories

### BeepBeepWin

this is a windows executable and our current preferred interface due to compile and run speeds

### BeepBeepApp

his is an android app that can be installed on other android devices.  It is useful, but when developing trajectories this is run in the Android Studio Device simulator.  Compile, launch and run times were less than ideal on some of our older machines so we transitioned to the Windows executable above.

In addition to the BeepBeep submodule you will need a module in your project that holds the trajectories that will be run by the simulator and the physical robot code.  This module is accessible to the BeepBeep and TeamCode making it simple to develop paths in the simulator and then quickly deploy to the physical robot with zero code changes\!

## Prerequisite

BeepBeep depends on the Action class in RoadRunner.  Please review the RR documentation for a better understanding of actions and SequentialAction  
[https://rr.brott.dev/docs/v1-0/actions/](https://rr.brott.dev/docs/v1-0/actions/)   
A full understand of Roadrunner is encouraged, please read all of their documentation.  BeepBeep examples show how to mirror trajectories from the Red to Blue side of the field allowing you to only have to maintain trajectories for the one side of the field.  When you find yourself needing to tweak the numbers between what should be identical paths, it is most likely an error in your RR setup/tuning or an incorrect start position for how you are physically placing your robot on the field.  We find RR accurate enough to run the same code on both sides of the field\!


## Installation into project:

1) We have provided a BeepBeep sammple example project.  PLEASE do not clone this sample project.  Use it for reference and you may pull sample files from it.  This sample project may not be kept up to date with RoadRunner or BeepBeep.  Clone those repos directly, not the sample project. [https://github.com/codeShareFTC/BeepBeepSampleProject]
2)  First you need a project that includes the FTC SDK and RoadRunner. See RR documentation at  [https://rr.brott.dev/docs/v1-0/installation/](https://rr.brott.dev/docs/v1-0/installation/) for details.
3) Make sure this project builds and RR is working
4) Fork BeepBeep into your own repo for easy modification and source control.
5) In your fork of BeepBeep go to \<\>code and copy the URL, use it in next step.
6) Add the BeepBeep Submodule - Select the terminal tab in the bottom window and enter the commend:

`Git submodule add https://github.com/your-repo/BeepBeep.git`

   ![addSubmodule](https://github.com/user-attachments/assets/1ab683f5-b537-4aa2-9d44-466a09269511)

7) Common version of RoadRunner - To ensure the same version of roadrunner is used by the simulator and the physical robot we need to define the version in a single location.  This is done at the top level of the project by creating a gradile file “build.RoardRunnerCommon.gradle” with the lines: (this file could be copied BeepBeep sample project)

    ```java
       repositories {
           maven { url = 'https://maven.brott.dev/' }
       }
       dependencies {
           implementation 'com.acmerobotics.roadrunner:core:1.0.0'
           implementation 'com.acmerobotics.roadrunner:actions:1.0.0'
       }
    ```
8) Any module that will use roadrunner should include this line in it’s gradle file (instead of above lines).  This will include build.gradle for TeamCode and BeepBeep
   `apply from: '../build.RoardRunnerCommon.gradle'`
9) Add a module to store the trajectories.  We named this TrajectoryActions.  This module should be at the same level as FTCRobotController and TeamCode.
   * use project view and add to top level. Clock on top level of project left window.  File->NewModule, and the files to GIT when prompted.
     
        
        ![AddModuleTrajectoryActions](https://github.com/user-attachments/assets/85bdda16-709f-4bc7-8f1a-d819afb64cf5)

     
   * edit TrajectoryActions build.gradle and add this line to to use our global roadrunner version
     `apply from: '../build.RoardRunnerCommon.gradle'`
     Change Java Version to “VERSION\_1\_8”
     
10) Copy sample files from TrajectoryActions directory into your projects TrajectoryActions directory. Add to GIT when prompted.
     [https://github.com/codeShareFTC/BeepBeepSampleProject/TrajectoryActions/]  Check this link!!!
11)  Edit settings.gradle (Project Setting)
    1) Add these  lines
     ```java 
       include ':BeepBeep:BeepBeepCore'
       include ':BeepBeep:BeepBeepWin'
       include ':BeepBeep:BeepBeepApp'
       include ':TrajectoryActions'
     ```
     
13) Synch project with gradle files to make sure project structure is updated.

## Clonning a project from GIT that already hass BeepBeep installed
If you have already added a BeepBeep to your project and have it it GitHub and now want to clone that repo to another computer do this section. 
1) Select the terminal tab in the bottom window and enter command
   `git submodule update --init`
2) Synch project with gradle files
3) git pull BeepBeep to confirm have latest files.

## Edit Configurations
1) add BeepBeepWin (windows executable, the prefered way)
    * Note the working directory must be `$MODULE_DIR$`
    ![EditConfigurationBeepBeepWin](https://github.com/user-attachments/assets/8f38cd03-9184-4fe6-ba40-70a10a72f4a2)


2) add BeepBeepApp (android app / phone simulator) if needed
    ![BeepBeepApp Add Configuration](https://github.com/user-attachments/assets/7fb1323c-a773-4b72-9212-b7b145998546)


## Changes to MecanumDrive 
For the physical robot to use the trajectory actions we need to make a few changes to the project
1) MecanumDrive must implement Drive template
    `public final class MecanumDrive implements Drive  {`
2) Drive will be read, hover over an `add dependency` and `import class`

We want to use common parameters for the simulator and the physical robot
1) Move the IMU config from the Params class to just be class variable
2) comment out or delete the PARAMS class
3) Instantiate PARAMS using the class shared with the simulator

```java
public class MecanumDrive implements Drive {
   // IMU orientation
   // TODO: fill in these values based on
   //   see https://ftc-docs.firstinspires.org/en/latest/programming_resources/imu/imu.html?highlight=imu#physical-hub-mounting
   public RevHubOrientationOnRobot.LogoFacingDirection logoFacingDirection =
           RevHubOrientationOnRobot.LogoFacingDirection.LEFT;
   public RevHubOrientationOnRobot.UsbFacingDirection usbFacingDirection =
           RevHubOrientationOnRobot.UsbFacingDirection.UP;

   public static ParamsMecanumDrive PARAMS = new ParamsMecanumDrive();

```

We need a few extra functions to allow setting and getting the robot position.  Put these at the end of the MecanumDrive class
```java
    public void setPose(Pose2d p) {this.pose = p;}   // Added for BeepBeep and TrajectoryAction compatibility
    public Pose2d getPose() {return this.pose;}      // Added for BeepBeep and TrajectoryAction compatibility
}
```
