#!/bin/bash
# Automated build + benchmark runner for Gallery perf optimization
# Usage: ./scripts/benchmark-loop.sh [label]
# Last modified: 2026-03-31

set -euo pipefail

LABEL="${1:-baseline}"
NAS_SRC="/Volumes/nas/PROJECTS/gallery/Gallery-1.13.1"
LOCAL_BUILD="/Users/mklod/Developer/gallery-local-build"
RESULTS_DIR="/Volumes/nas/PROJECTS/gallery/reports/benchmarks"
ADB="/Users/mklod/Library/Android/sdk/platform-tools/adb"
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
TIMESTAMP=$(date +%Y-%m-%d--%H%M)

echo "=== Gallery Benchmark: $LABEL ($TIMESTAMP) ==="

# 1. Sync source to local build dir
echo "[1/5] Syncing NAS → local..."
rsync -a --delete --exclude='.gradle' --exclude='build' \
  "$NAS_SRC/" "$LOCAL_BUILD/"

# 2. Run unit tests
echo "[2/5] Running unit tests..."
cd "$LOCAL_BUILD"
./gradlew testFossDebugUnitTest --no-daemon 2>&1 | tail -5

# 3. Build debug APK + test APK
echo "[3/5] Building APKs..."
./gradlew assembleFossDebug assembleFossDebugAndroidTest --no-daemon 2>&1 | tail -5

# 4. Install and run instrumented tests
echo "[4/5] Installing and running benchmarks..."
DEBUG_APK=$(ls app/build/outputs/apk/foss/debug/*-foss-debug.apk | head -1)
TEST_APK=$(ls app/build/outputs/apk/androidTest/foss/debug/*-foss-debug-androidTest.apk | head -1)
$ADB install -r "$DEBUG_APK"
$ADB install -r "$TEST_APK"
$ADB shell am instrument -w \
  -e class org.fossify.gallery.benchmark.AllMediaQueryBenchmark \
  org.fossify.gallery.debug.test/androidx.test.runner.AndroidJUnitRunner \
  2>&1 | tee "/tmp/benchmark-$LABEL.txt"

# 5. Save results
echo "[5/5] Saving results..."
mkdir -p "$RESULTS_DIR"
cp "/tmp/benchmark-$LABEL.txt" "$RESULTS_DIR/$TIMESTAMP-$LABEL.txt"
echo "Results saved to $RESULTS_DIR/$TIMESTAMP-$LABEL.txt"

# 6. Copy APK to NAS builds
cp "$DEBUG_APK" \
  "/Volumes/nas/PROJECTS/gallery/builds/$TIMESTAMP-$LABEL.apk"
echo "APK saved to builds/$TIMESTAMP-$LABEL.apk"
echo "=== Done ==="
