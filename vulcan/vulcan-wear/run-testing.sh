# !/bin/bash
# run-training.sh

# Run training using droidbot, either in the mobile or in the wearable device.
# Dependencies:
#  + run-test.sh
#
# author: ebarsallo
# Purdue University, DCSL, 2019


### Parameters
DROIDBOT=droidbot
OUTPUT=target/vanilla
# devices
source DEVICES
# default: wear
DEFAULT_WEAR=${WATCH2_3}
DEFAULT_WEAR_LABEL=${WATCH2_LABEL}
APK_WEAR=target/apks/wear/com.google.android.apps.fitness.apk
# default: mobile
DEFAULT_MOBILE=${NEXUS_5X_1}
DEFAULT_MOBILE_LABEL=${NEXUS_5X_LABEL}
APK_MOBILE=target/apks/mobile/com.google.android.apps.fitness.apk
# replay
REPLAY=target/vanilla/190726_174738-com.google.android.apps.fitness-watch2/

### Routines

# params
sanity_checks() {
    echo "No sanity checks"
}


### Main
# Sanity checks
sanity_checks
# arg: None (for now)

# running droidbot
# arg: Mobile or Wearable
arg1=$1
if [ "$arg1" == "mobile" ]; then
    mode=0
elif [ "$arg1" == "wear" ]; then
    mode=1
else
    echo "Invalid or missing mode"
    exit 1
fi

# running droidbot
case $mode in
    0)
        echo "Testing by steering events in the mobile"
        ./run-test.sh ${APK_MOBILE} ${DEFAULT_MOBILE_LABEL} 11 ${DEFAULT_MOBILE} ${DEFAULT_WEAR} ${REPLAY}
    ;;
    
    1)
        echo "Testing by steering events in wearable"
        ./run-test.sh ${APK_WEAR} ${DEFAULT_WEAR_LABEL} 12 ${DEFAULT_MOBILE} ${DEFAULT_WEAR} ${REPLAY}
    ;;
esac

exit 0
