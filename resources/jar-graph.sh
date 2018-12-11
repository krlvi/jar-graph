#!/bin/bash

JAR_EXEC=./bin/jar-graph-0.1.0-standalone.jar

if ! [ -x "$(command -v java)" ]; then
  echo 'Error: java is not installed.' >&2
  exit 1
elif ! [ -x "$(command -v jdeps)" ]; then
  echo 'Error: jdeps is not installed.' >&2
  exit 1
fi

usage()
{
  scriptname=$0
  echo "usage: $scriptname -f <jar_file> -p <package> [-t time] [-nhi] [-o out_file] [-w dir] [-r repulsion] [-s font] [-m min] [-x max]"
  echo "  -t  time      specify simulation time in seconds"
  echo "  -n            no graph will be generated"
  echo "  -h            display help"
  echo "  -o  outfile   output file outfile"
  echo "  -w  workdir   working directory"
  echo "  -r  repulsion node repulsion strength value"
  echo "  -s  size      font size"
  echo "  -m  min       minimum node size"
  echo "  -x  max       maximum node size"
  echo "  -i            list nodes and their in degree minus out degree"
}

run_jdeps()
{
  if [ -z "$3" ]; then
    jdeps -verbose:class -dotoutput $1 $2;
  else
    package=$(sed "s/\./\\\./g" <<<"$3")
    jdeps -f '^(?!'$package').+' -verbose:class -dotoutput $1 $2;
  fi
}

prepare_graph()
{
  if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i "" "/(not found)/d" $2;
    sed -i "" "s/ (${1})//g" $2;
  else
    sed -i "/(not found)/d" $2;
    sed -i "" "s/ (${1})//g" $2;
  fi
}

cleanup_workdir()
{
  rm -f $1
  rm -f $2
}

target_jar=
package=
simulation_time_sec=20
no_graph=
out_file=
work_dir=/tmp/jar-graph
node_repulsion_strength=800
label_font_size=2
node_min_size=6
node_max_size=40
list_nodes=

if [ $# -eq 0 ]; then
    usage
    exit 1
fi

while [ "$1" != "" ]; do
  case $1 in
    -f | --file )       shift
                        target_jar=$1
                        ;;
    -p | --package )    shift
                        package=$1
                        ;;
    -t | --time )       shift
                        simulation_time_sec=$1
                        ;;
    -n | --no-graph )   shift
                        no_graph=1
                        ;;
    -h | --help )       usage
                        exit
                        ;;
    -o | --out )        shift
                        out_file=$1
                        ;;
    -w | --workdir )    shift
                        work_dir=$1
                        ;;
    -r | --repulsion )  shift
                        node_repulsion_strength=$1
                        ;;
    -s | --font-size )  shift
                        label_font_size=$1
                        ;;
    -m | --min-node )   shift
                        node_min_size=$1
                        ;;
    -x | --max-node )   shift
                        node_max_size=$1
                        ;;
    -i | --list-nodes ) shift
                        list_nodes=1
                        ;;
    * )                 usage
                        exit
                        ;;
  esac
  shift
done

if [ ! -f $target_jar ]; then
    echo "File not found!"
    exit 1
fi

mkdir -p $work_dir

name=$(basename $target_jar)
dotfile="$work_dir/${name}.dot"
out_file=$(pwd)/${out_file:-$name".pdf"}

run_jdeps $work_dir $target_jar $package 
prepare_graph $name $dotfile

java -jar $JAR_EXEC\
 --dot-file $dotfile\
 --out-graph $out_file\
 --simulation-time $simulation_time_sec\
 --repulsion-strength $node_repulsion_strength\
 --min-size $node_min_size\
 --max-size $node_max_size\
 --label-size $label_font_size\
 ${no_graph:+"--skip-graph"}\
 ${list_nodes:+"--list-nodes"}

cleanup_workdir $dotfile "$work_dir/summary.dot"
