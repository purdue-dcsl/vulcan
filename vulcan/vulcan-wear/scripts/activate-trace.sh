#!/bin/bash
#
# Activate traces for all connected device (using adb).
#
# Usage: 
#   activate-trace.sh

activate() {
# Activate traces for device
# $1: device

  device_in=$1
  echo "[*] Activating trace for " ${device_in}
  adb -s ${device_in} shell setprop log.tag.DataMap VERBOSE
  adb -s ${device_in} shell setprop log.tag.WearableLS VERBOSE 
  adb -s ${device_in} shell setprop log.tag.NotifManCompat VERBOSE
  adb -s ${device_in} shell setprop log.tag.FirebaseMessaging VERBOSE  
 
  # This traces cannot be activated on devices
  # adb -s ${device_in} shell setprop ro.logd.filter disable
  # adb -s ${device_in} shell setprop persist.logd.filter disable

  adb -s ${device_in} logcat -c
  adb -s ${device_in} logcat -G 14M

}

# main
adb="$(adb devices | cut -d'	' -f1)"
devices=${adb#"List of devices attached"}

for device in $devices
do
   echo "Activating traces on ${device}"
   activate $device
done

echo "Success!"
exit 0