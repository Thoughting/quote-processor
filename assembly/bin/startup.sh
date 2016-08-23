#!/bin/bash

current_path=`pwd`
case "`uname`" in
    Linux)
		bin_abs_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_abs_path=`cd $(dirname $0); pwd`
		;;
esac
base=${bin_abs_path}/..
export LANG=en_US.UTF-8
export BASE=$base

if [ -f $base/bin/processor.pid ] ; then
	echo "found processor.pid , Please run stop.sh first ,then startup.sh" 2>&2
    exit 1
fi


## set java path
if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
echo $JAVA
fi


## set java opts

JAVA_OPTS="-server -Xms2560m -Xmx4096m -Xmn1536m -XX:SurvivorRatio=16 -XX:PermSize=128m -XX:MaxPermSize=800m -Xss256k -XX:+UseAdaptiveSizePolicy -XX:MaxTenuringThreshold=15 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:SoftRefLRUPolicyMSPerMB=0 -XX:+HeapDumpOnOutOfMemoryError -Djava.awt.headless=true"

JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"


## set classpath lib
	for i in $base/lib/*;
		do CLASSPATH=$i:"$CLASSPATH";
	done
 	CLASSPATH="$base:$base/conf:$CLASSPATH";
 	
  	cd $bin_abs_path
	echo CLASSPATH :$CLASSPATH
	$JAVA $JAVA_OPTS $JAVA_DEBUG_OPT  -classpath .:$CLASSPATH com.baidu.stock.process.Startup  &>/dev/null &
	echo $! > $base/bin/processor.pid 
	

