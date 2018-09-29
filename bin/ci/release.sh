#!/bin/bash
mkdir -p ./release/bin

#lein test;
lein uberjar;
mv ./target/uberjar/jar-graph-0.1.0-standalone.jar ./release/bin
cp ./resources/jar-graph.sh ./release/
zip -r release.zip ./release
