# burp2wfuzz

BurpSuite extension written in Java that converts caught http requests into wfuzz
commands ready to be pasted into a terminal. It combines the convenience 
of Burp with the speed of wfuzz. 

## Installation
- Download the .jar file and load it into extender in Burp.
- Or build it into .jar from source (preferably using gradle) and load into extender.

## Usage
Right-click on the request (editor or viewer) and 
select `Extensions > burp2wfuzz > Copy request as wfuzz command`