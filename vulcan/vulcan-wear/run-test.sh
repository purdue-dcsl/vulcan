# !/bin/bash
# run-test.sh [target_app] [label] [mode] [mobile_serial] [wearable_serial] [replay_dir]

# Run droidbot to explore an application installed on an Android compatible 
# device (Android or Wear OS)
# Dependencies:
#  + droidbot-wear
#
# author: ebarsallo
# Purdue University, DCSL, 2019


### Parameters
NONE=NONE
NOAPP=NOAPP
DROIDBOT=droidbot
OUTPUT=target/vanilla
# devices
source DEVICES
# wear
DEFAULT_WEAR=${TICWATCH_C2}
APK_WEAR=target/apks/wear/com.google.android.apps.fitness.apk
DEVICE_WEAR=${TICWATCH_C2_LABEL}
# mobile
DEFAULT_MOBILE=${NEXUS_5X_1}
APK_MOBILE=target/apks/mobile/com.google.android.apps.fitness.apk
DEFAULT_MOBILE_LABEL=${NEXUS_5X_1_LABEL}

### Routines

# Usage
usage() {
    echo "run-test.sh [target_app] [label] [mode] [mobile_serial] [wearable_serial]"
}

# Run an external command/script
run_command() {
    local __cmd=$1

    eval "${__cmd}"
    if [[ $? != 0 ]]; then
        echo "Error: Exiting program."
        exit 1
    fi
}

# Get the path for an specific app.
create_dir() {
    # args
    local __name=$1

    echo ${__name}
    mkdir ${OUTPUT}/${__name}
}

### Main

# TODO. If some day someone wants to implements something more sophisticated to
# parse the args, here are some ideas:
#   + https://stackoverflow.com/questions/14786984/best-way-to-parse-command-line-args-in-bash
#   + https://stackoverflow.com/questions/192249/how-do-i-parse-command-line-arguments-in-bash/14203146

echo "Running test ..."
echo " » Args received: [$1] [$2] [$3] [$4] [$5]"

# arg1: target application
if [ "$1" != "" ]; then
    app=$1
else
    app=${NOAPP}
fi

# arg2: label
# FIXME. This can be extracted from the device (droidbot does it)
if [ "$2" != "" ]; then
    device_label=$2
else
    device_label=${DEFAULT_MOBILE_LABEL}
fi

# arg3: exploration mode
if [ "$3" != "" ]; then
    choice=$3
else
    choice=2
fi

# arg4: target device #1
if [ "$4" != "" ]; then
    target1=$4
else
    target1=${DEFAULT_MOBILE}
fi

# arg5: target device #2
if [ "$5" != "" ]; then
    target2=$5
else
    target2=${DEFAULT_WEAR}
fi

# arg6: replay folder
if [ "$6" != "" ]; then
    replay_dir=$6
fi

echo " » Parameters: app [$app] label [$device_label] mode [$choice] target1 [$target1] target2 [$target2]"

# activate traces
echo " » Activating traces on connected devices"
run_command scripts/activate-trace.sh

package=`aapt dump badging ${app} | grep package | awk '{print $2}' | sed s/name=//g | sed s/\'//g`
now=$(date +"%y%m%d_%H%M%S")

case ${choice} in

    1)
        # exploration in mobile only
        folder=${now}-${package}-${device_label}
        create_dir ${folder}
        echo ${target1} ${app} 
        ${DROIDBOT} -d ${target1} -a ${app} -no_install -o ${OUTPUT}/${folder} |& tee ${OUTPUT}/${folder}/output.log
    ;;

    2)
        # exploration in mobile w/wearable paired
        folder=${now}-${package}-${device_label}
        create_dir ${folder}
        echo ${target1} ${target2} ${app}
        ${DROIDBOT} -d ${target1} -d2 ${target2} -wear 2 -a ${app} -no_install -o ${OUTPUT}/${folder} |& tee ${OUTPUT}/${folder}/output.log
    ;;

    3)
        # exploration in wearable only
        folder=${now}-${package}-${device_label}
        create_dir ${folder}
        echo ${target2} ${app} 
        ${DROIDBOT} -d ${target1} -wear 1 -a ${app} -no_install -o ${OUTPUT}/${folder} |& tee ${OUTPUT}/${folder}/output.log
    ;;

    4)
        # @deprecated 7/24/19 - DELETE THIS OPTION
        # replaying in wearable only (this is not actually helpful at all)
        folder=${now}-${package}-replay-${device_label}
        replay_folder=target/vanilla/190712_132053-com.google.android.apps.fitness-watch2
        create_dir ${folder}
        echo ${target1} ${app} 
        ${DROIDBOT} -d ${target2} -wear 1 -a ${APK_WEAR} -no_install -policy replay -replay_output ${replay_folder} |& tee ${OUTPUT}/${folder}/output.log
    ;;


    11)
        # testing (training in mobile)
        folder=${now}-${package}-${device_label}-testing
        create_dir ${folder}
        echo ${target1} ${app} 
        python -m bork.start_orchestrator -wear 2 -d ${target1} -replay_output ${replay_dir} -a ${app} -o ${OUTPUT}/${folder} |& tee ${OUTPUT}/${folder}/output.log
    ;;        

    12)
        # testing (training in wearable)
        folder=${now}-${package}-${device_label}-testing
        create_dir ${folder}
        echo ${target1} ${app} 
        python -m bork.start_orchestrator -wear 1 -d ${target2} -replay_output ${replay_dir} -a ${app} -o ${OUTPUT}/${folder} |& tee ${OUTPUT}/${folder}/output.log
    ;; 

    *)
        # default
        echo "Error: mode ${choice} is not a valid option."
    ;;


esac

