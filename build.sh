#! /bin/bash

dependencies=(git gradle java npm node)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

check (){
  for i in "${dependencies[@]}";
  do
    if ! command -v "$i" &> /dev/null; then
      echo "$i could not be found"
      exit
    fi
  done
}

update_jar () {
  cd $DIR
  cd hapi
  git checkout -B visualizer origin/visualizer
  git pull origin visualizer

  gradle build-all-tools --no-daemon

  cd $DIR
  mv hapi/build/libs/* bin/
}

from_scratch () {
  cd $DIR
  rm yes | rm -r hapi
  git clone https://github.com/lac-dcc/hapi.git
  cd hapi
  git checkout -b visualizer origin/visualizer
  update_jar
}

help_msg () {
  echo "Hapi visualizer builder."
  echo
  echo "Usage: bash build.sh <[COMMAND] | [FLAG]>"
  echo "COMMAND":
  echo "run               Starts the nodejs server"
  echo
  echo "FLAG:"
  echo "--update          Pull code from the lastest version of hapi repo and rebuild"
  echo "                  the .jar ."
  echo "--from-scratch    First build of the hapi visualizer."
  echo
  exit
}

start_nodejs () {
  cd $DIR
  npm install
  npm start
}

flags () {
  case $1 in
    --update) update_jar ;;
    --from-scratch) from_scratch;;
    *) help_msg ;;
  esac
}

main () {
  check dependencies

  # frist argument is not null
  if [ "$1" = "run" ]; then
    if ! [ -z "$2" ]; then
      flags $2
    fi
    start_nodejs
  elif ! [ -z "$1" ]; then
    flags $1
  else
    help_msg
  fi
}

main $@