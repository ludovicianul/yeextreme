# Yeextreme #
In the era of overwhelming digital notifications (Slack, Teams, Mobile Phones, Emails, etc) I'm a strong believer in visual and instant feedback.
This is why I think Extreme Feedback Devices are essential for every development team. Especially in distributed scenarios when you have lots of teams working on the same codebase.

**Yeextreme** is a simple application than can transform a [Xiaomi Yeelight RGB light bulb](https://www.yeelight.com/en_US/product/wifi-led-c) into a configurable eXtreme Feedback Device.

# Why not use a simple CI server plugin #
This app has some flexible ways to monitor and define color states and Continuous Integration (CI) jobs. You can:

- define time-based monitoring for different CI jobs
- have simple time-based events that display a fix color (useful for team events)
- define custom colors associated to CI jobs or events
- some plugins integrate with IFTT webhooks which sometimes are unreliable or not configurable

# Configuration #
**Yeextreme** is driven by a simple properties configuration file. The file can contain the following items:

- CI tasks: with the following syntax: ```task_ciServer_taskName=startTime,endTime,ciServerApiUrl```
- Event tasks: with the following syntax: ```task_eventName=startTime,endTime,color```
    - ```startTime``` and ```endTime``` are valid time of the day values in format ```HH:mm```
    - ```color``` is a color name that **must** also be defined as a property (see below)
    - ```ciServerApiUrl``` is the URL that will be called by the build extractor in order to get the build status
- Custom color: with the following syntax: ```c_colorName=red,green,blue,brightness,pulse```, where
    - ```red,green,blue``` are ```int``` with values between ```0``` and ```255```
    - ```brightness``` is also an ```int``` with values between ```0``` and ```100```
    - ```pulse``` is a ```boolean``` saying if this is a pulsing color or not; valid values are ```true``` or ```false```
    
- Properties controlling different aspects:
    - ```yeelightIP``` the IP of the Yeelight light bulb
    - ```yeelightPort``` the port of the Yeelight light bulb. This makes sense only if the standard port is not used. You can usually omit this
    - ```maxYeeLightConnectionAttempts``` how many tries will the app retry in case of a fail communication with the light bulb. Default is 9
    - ```telnetPort``` the port where the app will listen for remote commands
    - ```secondsBetweenChecks``` time to pause between re-assessing the supplied tasks. Default is 15 seconds
    
You can find a sample configuration file in the repo: ```yeextreme.properties```.

# Reserved Colors #
The following colors are reserved for the CI build status:
- ```RED``` for failed builds
- ```GREEN``` for passed builds
- ```BLUE``` for build in progress
- ````YELLOW```` for unstable builds
- ```ALL``` for signaling communication errors

These colors does not need to be defined in the properties file as they default to the corresponding real color.
**But** you can also override each of these colors using ```c_``` properties: ```c_red=r,g,b,brightness,pulse```.

```ALL``` will make the device pulse into a pinky color in order to signal a communication problem.

# Task priorities #
The shorter time intervals will take priority over longer time intervals. 
For example if you have a task configured between 10:00 and 18:00 and another one between 10:15-10:45, the later will take priority during that interval.
The monitoring will resume to the initial task afterwards.

# always_monitor task #
You can also configure a special task called ```always_monitor``` with the following syntax: ```COLOR,URL```. 
This will be checked for availability periodically and every time the URL is down the light bulb will turn into the specified color overriding any existing setup.
This is very useful when you have some critical resources you want to get notified about.

# Limitations #
**Yeextreme** only works with Jenkins for now. It is essential that all CI tasks must contain ```jenkins``` as the ```ciServer```.
Otherwise the job won't be monitored.

**Yeextreme** only considers Time of Day ignoring day and month related information.

# Pre-requisites #
**Yeextreme** must run on the **same network** as the Yeelight RGB light bulb. 

The Yeelight RGB light bulb **must first be configured to have WiFi access** (using the Yeelight app from the Play Store).

The Yeelight RGB light bulb **must have LAN control** enabled (using the Yeelight app from the Play Store).

You must have Java 8+ installed on the box running **Yeextreme**.

# Remote control #
There are situations when you will run **Yeextreme** on a remote box. You can telnet on the configured telnet port (default is 8888) and run the following commands:

- ```ping``` - if you get ```pong``` this means that the device is running healthy
- ```state``` - will return what Task is currently considered the best candidate for monitoring
- ```colors``` - will return the valid colors configured in the properties file
- ```reloadProps URL``` - will trigger a properties reload from the supplied URL. This helps reloading the properties at runtime, without restarting the application
- ```quit``` - will close the connection

# Running the app ##
**Yeextreme** is built as an executable jar. Just do: ```./yeelight-1.2.jar``` (or ```java -jar yeelight-1.2.jar```).
Check the Releases tab for latest version. Latest version here: https://github.com/ludovicianul/yeextreme/releases/download/v1.2/yeextreme-1.2.jar


# Build from sources #
You must first run ```mvn initialize``` in order to deploy the ```yapi``` in library in the local repo.
Run a ```mvn clean package```. It will output an executable jar in the current folder. (of course, you need Maven installed on the build box).

# Thanks #
- https://github.com/florian-mollin/yapi for the Yeelight Java API
