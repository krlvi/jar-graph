#!/bin/bash
mkdir -p ./jar-graph-release/bin

#lein test;
lein uberjar;
mv ./target/uberjar/jar-graph-0.1.0-standalone.jar ./jar-graph-release/bin
cp ./resources/jar-graph.sh ./jar-graph-release/
zip -r jar-graph-release.zip ./jar-graph-release
