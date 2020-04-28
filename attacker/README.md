
## Instructions

 1. Create `logs` directory. This file will store all the traces collected by `active-trace.sh`.
 2. Modify ryloth/attacker app (branch: `attacker-bg`) before running the experiment:

 * Modify the sensor delay in the file: fuzzer/src/main/java/edu/purdue/dagobah/common/Constant.h

```
    /** sensors */
    // SensorManager.SENSOR_DELAY_FASTEST:     0ms
    // SensorManager.SENSOR_DELAY_GAME:       20ms
    // SensorManager.SENSOR_DELAY_DELAY_UI:   67ms
    // SensorManager.SENSOR_DELAY_NORMAL:    200ms
    public static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;
```
 * (just in case) The sensors set activated by the attacker app are in: app/src/main/java/edu/purdue/ryloth/services/sensor/SensorMan.java.

```
        // fossil: sensors
        gTASensors.put (SENSOR_TYPE_1 , true);
        gTASensors.put (SENSOR_TYPE_2 , true);
        gTASensors.put (SENSOR_TYPE_3 , true);
        gTASensors.put (SENSOR_TYPE_4 , true);
        gTASensors.put (SENSOR_TYPE_5 , true);
        gTASensors.put (SENSOR_TYPE_6 , true);
        gTASensors.put (SENSOR_TYPE_8 , true);
        gTASensors.put (SENSOR_TYPE_9 , true);
        gTASensors.put (SENSOR_TYPE_10, true);
        gTASensors.put (SENSOR_TYPE_11, true);
        gTASensors.put (SENSOR_TYPE_14, true);
        gTASensors.put (SENSOR_TYPE_17, true);
        gTASensors.put (SENSOR_TYPE_18, true);
        gTASensors.put (SENSOR_TYPE_19, true);
        gTASensors.put (SENSOR_TYPE_20, true);
        gTASensors.put (SENSOR_TYPE_21, true);
        gTASensors.put (SENSOR_TYPE_26, true);
        //gTASensors.put (SENSOR_TYPE_29, true);
        //gTASensors.put (SENSOR_TYPE_30, true);
        gTASensors.put (SENSOR_TYPE_34, true);
        gTASensors.put (SENSOR_TYPE_65572, true);
        gTASensors.put (SENSOR_TYPE_65536, true);
```

3. Run `active-trace.sh` to setup the devices, you should get an output like the following:

```
$ scripts/activate-trace.sh
--
--> 127.0.0.1:4448 device 8XV5T16219002827 device
--
Activating traces on 127.0.0.1:4448
Activating traces on device
Activating traces on 8XV5T16219002827
[*] Setting up bt debug connection for watch paired to  8XV5T16219002827  on port  4448
already connected to 127.0.0.1:4448

----------------------------------------------------
List of devices attached
127.0.0.1:4448         device product:darter model:Fossil_Sport device:darter transport_id:47
8XV5T16219002827       device usb:1-3 product:angler model:Nexus_6P device:angler transport_id:46

----------------------------------------------------
Success!

```
4. Create a session variable, `SERIAL`, and assign the serial of the target device.

```
$ export SERIAL=127.0.0.1:4448

```

5. Run `collect-trace.sh` in one tab with a label of the log name as argument.

```
$ scripts/collect-trace.sh sensor-5-five-all-delay_normal-trial-14

```

6. Install the attacker app using Android Studio.
7. Wait...
8. Wait...
9. When the device reboots, then run again `active-trace.sh` until both devices are connected.
10. Run `collect-trace.sh` again this time with a second argument: false.

```
$ scripts/collect-trace.sh sensor-5-five-all-delay_normal-trial-14--after-reboot false

```
11. To check intents/sensor-reads in the log, you can use the last entry (before the reboot) of: ryloth/mm. For example:

```
12-06 12:50:39.623 12600 12654 D ryloth/mm: 466820ms (361/70709) -> java.lang.RuntimeException: android.os.DeadSystemException
```

The first value in the parenthesis is the # of intent, the second is the sensor reads.
