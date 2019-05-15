#!/bin/sh
###############################################################################
##         (c) Copyright, Real-Time Innovations, All rights reserved.        ##
##                                                                           ##
##         Permission to modify and use for internal purposes granted.       ##
## This software is provided "as is", without warranty, express or implied.  ##
##                                                                           ##
###############################################################################

# You can override the following settings with the correct location of Java
JAVA=`which java`

# Make sure JAVA and NDDSHOME are set correctly
test -z "$JAVA" && echo "java not found" && exit 0
test -z "$NDDSHOME" && echo "NDDSHOME environment variable not set!" && exit 0

# Ensure this script is invoked from the root directory of the project
test ! -d src && echo "You must run this script from the example root directory" && exit 0

# Set RTI_JAVA_OPTION to -d64 if you are running on 64-bit system using 64-bit
# libraries

# Run example
$JAVA $RTI_JAVA_OPTION -classpath objs:"$NDDSHOME/lib/java/nddsjava.jar" PublisherManager "$@"
