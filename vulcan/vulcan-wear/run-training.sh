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

### Routines

# params
sanity_checks() {
    # Display PARAMS
    echo $DEFAULT_WEAR $DEFAULT_WEAR_LABEL $APK_WEAR
    echo $DEFAULT_MOBILE $DEFAULT_MOBILE_LABEL $APK_MOBILE

    # Check PARAMS
    if [ "${APK_MOBILE}" == "" ]; then
        echo "Error: Please check APK_MOBILE value."
        exit 1
    fi
    # if [ "${DEFAULT_MOBILE}" == "" ]; then
    #     echo "Error: Please check DEFAULT_MOBILE value."
    #     exit 1
    # fi
    if [ "${DEFAULT_MOBILE_LABEL}" == "" ]; then
        echo "Error: Please check DEFAULT_MOBILE_LABEL value."
        exit 1
    fi
    if [ "${APK_WEAR}" == "" ]; then
        echo "Error: Please check APK_WEAR value."
        exit 1
    fi
    if [ "${DEFAULT_WEAR}" == "" ]; then
        echo "Error: Please check DEFAULT_WEAR value."
        exit 1
    fi
    if [ "${DEFAULT_WEAR_LABEL}" == "" ]; then
        echo "Error: Please check DEFAULT_WEAR_LABEL value."
        exit 1
    fi
}


### Main
# Sanity checks
sanity_checks
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
        echo "Training in Mobile"
        ./run-test.sh ${APK_MOBILE} ${DEFAULT_MOBILE_LABEL} 2 ${DEFAULT_MOBILE} ${DEFAULT_WEAR} 
    ;;
    
    1)
        echo "Training in Wearable"
        ./run-test.sh ${APK_WEAR} ${DEFAULT_WEAR_LABEL} 3 ${DEFAULT_WEAR} 
    ;;
esac

exit 0
