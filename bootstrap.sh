#!/bin/sh

ARGS=$@

(
  cd bootstrap/bootstrap-mini
  ./build
  ret=$?; if [ $ret != 0 ]; then exit $ret; fi
  java -jar target/bootstrap-mini.jar install $ARGS
  ret=$?; if [ $ret != 0 ]; then exit $ret; fi
)
ret=$?; if [ $ret != 0 ]; then exit $ret; fi

BOOTSTRAP_JAR=bootstrap-mini/target/bootstrap-mini-2.0.1-SNAPSHOT.jar

(
  cd bootstrap/bootstrap-installer
  java -jar ../$BOOTSTRAP_JAR package $ARGS
  ret=$?; if [ $ret != 0 ]; then exit $ret; fi
)
ret=$?; if [ $ret != 0 ]; then exit $ret; fi

# TODO: get rid of M2_HOME?
java -jar bootstrap/bootstrap-installer/target/bootstrap-installer-2.0.1-SNAPSHOT.jar --prefix=`dirname $M2_HOME` $ARGS
ret=$?; if [ $ret != 0 ]; then exit $ret; fi

(
  # TODO: should w ebe going back to the mini now that we have the real thing?
  cd maven-core-it-verifier
  java -jar ../bootstrap/$BOOTSTRAP_JAR package $ARGS
  ret=$?; if [ $ret != 0 ]; then exit $ret; fi
)
ret=$?; if [ $ret != 0 ]; then exit $ret; fi

(
  cd ./maven-core-it
  echo
  echo "Running maven-core integration tests ..."
  echo 
  ./maven-core-it.sh $ARGS
  ret=$?; if [ $ret != 0 ]; then exit $ret; fi
)
ret=$?; if [ $ret != 0 ]; then exit $ret; fi
