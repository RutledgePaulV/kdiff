#!/usr/bin/env bash
set -e
lein build
cp -f target/kdiff /usr/local/bin/kdiff
cp -f target/kdiff bin/darwin/kdiff