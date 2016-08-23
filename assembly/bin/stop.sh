#!/bin/bash


get_pid() {	
	STR=$1
	PID=$2
        if [ ! -z "$PID" ]; then
        	JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep "$PID"|grep -v grep|awk '{print $2}'`
	    else 
	        JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep -v grep|awk '{print $2}'`
        fi

    echo $JAVA_PID;
}

base=`dirname $0`/..
pidfile=$base/bin/processor.pid
if [ ! -f "$pidfile" ];then
	echo "processor is not running. exists"
	exit
fi

pid=`cat $pidfile`
if [ "$pid" == "" ] ; then
	pid=`get_pid "appName=processor"`
fi

echo -e "`hostname`: stopping processor $pid ... "
kill -9 $pid

LOOPS=0
while (true); 
do 
	gpid=`get_pid "appName=processor" "$pid"`
    if [ "$gpid" == "" ] ; then
    	echo "ok!"
    	`rm $pidfile`
    	break;
    fi
    let LOOPS=LOOPS+1
    sleep 1
done