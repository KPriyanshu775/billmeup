#!/bin/zsh
cd "/Users/priyanshukumar/Documents/New project" || exit 1
javac -d out src/billmeup/*.java
java -cp out billmeup.Main
