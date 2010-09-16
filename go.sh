#!/bin/sh

if [ "$1" = "" ]; then
    DST=5554
else
    DST=$1
fi

#ant debug && adb -s emulator-$DST install -r bin/*-debug.apk
ant release && scp bin/Pascal-release.apk orbitel.ru:www/p.apk
