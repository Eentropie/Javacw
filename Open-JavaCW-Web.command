#!/bin/zsh
cd "$(dirname "$0")" || exit 1

echo "Compiling JavaCW..."
javac -d out $(find src -name '*.java')
if [ $? -ne 0 ]; then
  echo
  echo "Compilation failed. Check the errors above."
  read -r "?Press Return to close."
  exit 1
fi

echo
echo "Starting JavaCW web frontend..."
java -cp out web.WebMain
