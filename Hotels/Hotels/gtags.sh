#!/bin/sh
find src -name '*.java' >GFILES
gtags -f GFILES
