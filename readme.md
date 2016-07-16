Hybrid Fault Tree Analysis Tool
===============================

This tool has been developed in context of the lecture "Functional Safety and System Dependability" (RWTH Aachen).

How to use
----------

It is a command line based tool. Simply call the built jar with the location of the hybrid fault tree file as argument. 

Provided the jar and the json file are in the same directory, use for example:

```
java -jar fart_fssd.jar HFTTestCase.json
```

After finishing the analysis your browser will show up with the plotted result.

Python 3 (https://www.python.org/) with plotly installed (https://plot.ly/python/getting-started/) is required for plotting the results.

How to build
------------

The easiest way is to open this project in IntelliJ (https://www.jetbrains.com/idea/) and use Build/Build Artifacts... in order to build the jar.

The outputted artifacts can be found in the folder out/artifacts/fart_fssd_jar
