#! /bin/bash

# Use this command to filter out only those lines that contain the
# summarized results that I care about for one of the raw dump files.

egrep '(^params: |Execution time mean : )' $*
