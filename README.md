# jar-graph

[![Build Status](https://travis-ci.org/krlvi/jar-graph.svg?branch=master)](https://travis-ci.org/krlvi/jar-graph)

Graph analysis of the internal dependencies within a java archive file (JAR) using the [gephi toolkit](https://github.com/gephi/gephi-toolkit).

As an example below is a visualization of the [Guava](https://github.com/google/guava) internal dependencies. Each node is a class, and edges represent an "imports" dependency. The node size is relative to the in-degree or the number of dependent nodes.
![guava graph](./examples/guava-26.0-jre.jar.pdf.jpg?raw=true)
## Installation

Required is Java 8 or later. Download [the latest release](https://github.com/krlvi/jar-graph/releases/latest) and extract the archive.

## Usage
```
usage: ./jar-graph.sh -f <jar_file> -p <package> [-t time] [-nh] [-o out_file] [-w dir] [-r repulsion] [-s font] [-m min] [-x max]
  -t  time      specify simulation time in seconds
  -n            no graph will be generated
  -h            display help
  -o  outfile   output file outfile
  -w  workdir   working directory
  -r  repulsion node repulsion strength value
  -s  size      font size
  -m  min       minimum node size
  -x  max       maximum node size
```

## Examples
### Guava
`./jar-graph.sh -f ../guava-26.0-jre.jar -p com.google -t 60`
In this example the script is executed on the [Guava 26.0 JAR](https://github.com/google/guava/releases/tag/v26.0) with a filter of "com.google" which indicates that the analysis will include only classes under the "com.google" package.
The simulation is ran for 60 seconds.
The resulting PDF can be explored [here](./examples/guava-26.0-jre.jar.pdf?raw=true).

### Elasticsearch
`./jar-graph.sh -f ../elasticsearch-6.4.1.jar -p org.elasticsearch.common -t 180`
This performs analysis of the [Elasticsearch 6.4.1 release](https://mvnrepository.com/artifact/org.elasticsearch/elasticsearch/6.4.1), limited to package "org.elasticsearch.common". For large projects it is often meaningful to analyze sub-parts separately otherwise resulting graphs may be very "busy".
The PDF can be explored [here](./examples/elasticsearch-6.4.1.jar.pdf?raw=true).

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
