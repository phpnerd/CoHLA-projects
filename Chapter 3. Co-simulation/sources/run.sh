#! /bin/sh
OPTIND=1 

XTERM=gnome-terminal

roomModel=../models/RoomModel.fmu
thermostatModel=../models/Thermostat.poosl
stepSize=1
lookahead=1
hostname=localhost
port=9999
measureTime=3600
log4j=log4j2.xml
showGUI=0
performBuild=0
startSimulation=0

while getopts "?r:t:s:l:h:p:m:l:gbe" opt; do
    case "$opt" in
    \?) echo "===== HELP =====
The following options are available.
  -r x: Set room model x.
  -t x: Set thermostat model x.
  -s x: Set the step size for the room to x.
  -l x: Set the lookahead for the room to x.
  -h x: Set the hostname for the thermostat to x.
  -p x: Set the port for the thermostat to x.
  -m x: Set the time to record the trace to csv to x.
  -l x: Set the log4j2.xml location to x.
  -g  : If set, the GUI is started as well.
  -b  : If set, a build will be done.
  -e  : If set, the simulations will be started."
  		exit ;;
    r) roomModel=$OPTARG ;;
    t) thermostatModel=$OPTARG ;;
    s) stepSize=$OPTARG ;;
    l) lookahead=$OPTARG ;;
    h) hostname=$OPTARG ;;
    p) port=$OPTARG ;;
    m) measureTime=$OPTARG ;;
    l) log4j=$OPTARG ;;
    g) showGUI=1 ;;
    b) performBuild=1 ;;
    e) startSimulation=1 ;;
    esac
done

if [ $performBuild -eq 1 ]; then
	echo "Building wrappers..."
	gradle :RoomFMIWrapper:build :RoomFMIWrapper:installDist :ThermostatPOOSLWrapper:build :ThermostatPOOSLWrapper:installDist
fi

if [ $startSimulation -eq 1 ]; then
	echo "Starting simulations..."
	$XTERM --tab -e "./RoomFMIWrapper/build/install/RoomFMIWrapper/bin/RoomFMIWrapper $roomModel $stepSize $lookahead $measureTime $log4j" \
		--tab -e "./ThermostatPOOSLWrapper/build/install/ThermostatPOOSLWrapper/bin/ThermostatPOOSLWrapper $hostname $port $measureTime $log4j" \
		--tab -e "rotalumis -f $thermostatModel"

	if [ $showGUI -eq 1 ]; then
		sleep 10s
		echo "Starting GUIs..."
		$XTERM --tab -e "gradle :RoomGUI:run" \
			--tab -e "gradle :ThermostatGUI:run"
	fi
fi
