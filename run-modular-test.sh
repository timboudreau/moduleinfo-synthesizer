#!/bin/bash

java \
  --module-path \
  test-projects/test-combined-modular-lib/target/test-combined-modular-lib-1.0.2.jar \
  -m \
  org.blerg.blarg/com.mastfrog.test.combined.TestCombinedModularLib


