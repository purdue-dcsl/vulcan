## REQUIREMENTS

* Python (both 2 and 3 are supported)
 * Java
 * Android SDK
 * Add platform_tools directory in Android SDK to PATH
   (adb is located in platform_tools)


## SETUP

 1. git clone the project
 

 2. Create virtual environment.

```   
cd droidbot-wear/
virtualenv env -p /usr/bin/python3
```

 3. Activate virtual environment.

```
source env/bin/activate
```

4. Install droidbot

```
pip install -e .
```


## ADDITIONAL (emmulator)

 1. Create emulator from Android Studio (with API 28)

 2. Start emulator

```
# replace Wear_OS_Round_API_28 with the name of your emulator
emulator -avd Wear_OS_Round_API_28 -no-snapshot-load -netdelay none -netspeed full
```

 3. Pair emulator with mobile phone. There is a step by step tutorial in the following link: http://www.technotalkative.com/android-wear-part-3-set-up-wear-emulator/

## ADDITIONAL (IDE Tools)

1. For pyCharm make sure to point to the enviroment created above. Go to `Settings > Project: droidbot-wear > Project interpreter` and select the `python environment`.

## TROUBLESHOOTING

 1. The android devices are not detected by the `adb` because of lack of permissions. This can be easily fixed by running `adb` with root priviledges, but this more a workaround than an actual solution. Common error messages include the following:

```
error: insufficient permissions for device: udev requires plugdev group membership 
no permissions (user in plugdev group; are your udev rules wrong?); see [http://developer.android.com/tools/device.html]
```

 * First, you need to add your username to the `plugdev` group:

```
sudo usermod -aG plugdev $LOGNAME
```

 * Second, you need update the `udev` rules. You can either manually add the rules -- which is really tedious -- (as mentioned [here](https://android.stackexchange.com/questions/122644/getting-and-no-permissions-when-using-adb-devices-on-linux/195443#195443)). However, you can also get a set of `udev` community-maintained default set of rules (as suggested [here](https://developer.android.com/studio/run/device))
