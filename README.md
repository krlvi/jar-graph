# jar-graph

[![Build Status](https://travis-ci.org/krlvi/jar-graph.svg?branch=master)](https://travis-ci.org/krlvi/jar-graph)

Graph analysis of the internal dependencies within a java archive file (JAR).

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



## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
