# Status

## Current milestone
Stage 7: Correctness — stale delete/move fix, round 2 (testing build 2026-07-08--0312)

## Last session (2026-07-08)
- Device testing of build 2026-07-02--1647 found two remaining ghost paths:
  1. Moved items stayed visible+selected in source folder until a slow full rescan finished → move flow now removes items from the adapter immediately (delete's mechanism: media.removeAll + removeSelectedItems, captured pre-op).
  2. Move to out-of-scope folder: ghosts in All Media with **destination** paths ("source and dest cannot be the same" on re-move proved rows were already re-pathed). Two writers were re-creating the rows after my cleanup deleted them: `rescanFolderMediaSync(destination)` (scans/insertAlls regardless of scope — now purges cached rows for out-of-scope folders instead) and the MediaStore ContentObserver `addPathToDB` (now skips files whose parent is out of scope).
- getCachedMedia SHOW_ALL now filters by full scan scope via `isFolderInGalleryScope` (excluded+hidden+.nomedia, memoized per parentPath) instead of excluded-config only — stray out-of-scope rows can never surface in All Media again. Preserves child-exclude-overrides-parent-include (shouldFolderBeVisible checks exclusion first) and temporarilyShowExcluded/Hidden modes.
- "Still laggy" reported — Stage 6 perf checklist remains open; folder rescan latency is what made these ghosts so visible.

## Last session (2026-07-02)
- Root-caused "deleted/moved items linger in gallery view" (the 2026-03-26 fix was incomplete — it only cleared MediaFetcher's 30s metadata HashMaps):
  1. Rescans overlapping a delete/move re-imported stale rows: scans read MediaStore (lags disk after in-app delete/rename) or pre-op snapshots (MainActivity's `android11Files`), then `INSERT OR REPLACE` into Room + overwrite `mMedia`, resurrecting items and even clearing the recycle-bin `deleted_ts` marker.
  2. Move never purged old paths from MediaStore (only `rescanPaths(newPaths)`) → every later scan re-imported the moved-away files; fullscreen-move did no source reconciliation at all.
  3. Move to an out-of-scope folder left re-pathed phantom rows in Room that cached All Media views showed at next app start.
- Fix: new `helpers/MediaTombstones.kt` — 60s tombstones for just-deleted/moved paths, filtered at every scan→cache write site (MediaActivity.gotMedia, MainActivity.gotDirectories ×2, rescanFolderMediaSync, getCachedMedia, ViewPagerActivity.gotMedia); registered in deleteFilteredFiles / movePathsInRecycleBin / ViewPager delete / both move flows; cleared on recycle-bin restore + addPathToDB + move-in.
- Move flows now: `rescanPaths(oldPaths)` (purges MediaStore rows after rename), `updateDBMediaPath` re-path, and `deleteMediumPath` if destination fails `shouldFolderBeVisible` (new `Context.isFolderInGalleryScope`).
- Added MediaTombstonesTest (6 unit tests, green). Full unit suite green. Built 2026-07-02--1647-stale-items-fix.apk → builds/.

## Next immediate task
- User-test build 2026-07-02--1647 against the CHANGELOG testing checklist (delete during cold-start scan, move to .nomedia folder, move-back-within-60s, recycle-bin restore)
- Then resume Stage 6 perf verification (build 2026-03-31--0140 checklist items still open)

## Blockers
- Gradle cannot build directly on NAS (file hashing fails over network FS) — workaround: rsync to local, build, copy APK back

## Key decisions
- Tombstone TTL 60s: long enough to outlive any in-flight scan + MediaStore rescan; short enough that a same-path file recreated later isn't hidden long. Explicit clears handle restore/move-back within the window.
- Deliberately did NOT tombstone inside `deleteDBPath` itself — reconciliation code calls it with object-equality diffs that can spuriously target existing files; tombstones are registered only at explicit user-operation sites.
- Move keeps `updateDBMediaPath` row re-path (favorite continuity) and drops the row afterwards only when the destination is out of gallery scope.

## Last session (2026-03-31)
- Implemented 5 performance optimizations (P0-P4):
  - P0: Thumbnail `.override(gridCellSize)` — decode at grid cell size, not full res
  - P1: Glide RecyclerViewPreloader — scroll-aware prefetch, removed manual 30-item preload
  - P2: Adapter reuse on view toggle — no more null+recreate for 4500 items
  - P3: Parallel folder scanning — 4-thread pool in GetMediaAsynctask
  - P4: Room WAL mode — concurrent reads during writes
- Added test infrastructure: JUnit 4, Espresso, AndroidX Benchmark, Robolectric
- Created benchmark-loop.sh script for automated build+test+benchmark
- Created Pixel 7 API 36 emulator for testing
- Verified Obsidian 2-way sync working (file watcher on Win10)
- Built and installed perf-optimized APK (builds/2026-03-31--0140-perf-optimized.apk)

## Next immediate task
- Test Build 2026-03-31--0140 on emulator (all testing checklist items)
- If perf is good, test on physical device
- Address TODO: view changer icon should show current view, not next view
- Consider: baseline profiles, startup tracing if still needed

## Blockers
- Gradle cannot build directly on NAS (file hashing fails over network FS) — workaround: rsync to local, build, copy APK back

## Key decisions
- Thumbnail override uses grid cell size calculated from screen width / column count / spacing
- RecyclerViewPreloader lookahead set to 15 items (tunable)
- Parallel scan uses 4 threads with separate MediaFetcher per thread (avoids shared mutable state)
- Adapter reuse triggers getMedia() in background for re-grouping after view toggle

## Last session (2026-03-26)
- Removed Videos virtual folder — only "All media" remains as virtual folder
- Fixed `getCachedMedia` for SHOW_ALL: single bulk DB query instead of per-folder scan
- Fixed pull-to-refresh crash (rescan-skip early return wasn't stopping refresh indicator)
- Fixed deleted items lingering in gallery view (cache invalidation after deletion)
- Fixed stale folder thumbnails (reset scan IDs on resume)
- Added `isDestroyed`/`isFinishing` guards on runOnUiThread blocks
- Removed all upstream GitHub Actions workflows and dependabot config
- Closed 6 dependabot PRs
- Pushed to https://github.com/mklod/gallery-android
- Created CHANGELOG.md with retroactive build entries
- Moved WORKPLAN.md into project root
- Created global ~/.claude/CLAUDE.md on Mac Mini
- Established local build workflow: edit on NAS → rsync to local → build → export APK to NAS builds/

## Next immediate task
- Test Build 2026-03-26--2015 (deletion fixes, pull-to-refresh crash fix)
- Continue performance brainstorm items if still laggy after testing
- Consider: lazy MediaStore refresh, parallel folder scanning, Glide RecyclerView preloader

## Blockers
- Gradle cannot build directly on NAS (file hashing fails over network FS) — workaround: rsync to local, build, copy APK back

## Key decisions
- Videos virtual folder removed — adds complexity for marginal value. Only "All media" remains.
- Build workflow: NAS source → rsync to /Users/mklod/Developer/gallery-local-build/ → Gradle build → APK to NAS builds/
- Rescan-skip optimization resets on every onResume (so returning from other activities always gets fresh data), only skips during background polling within same session
- No emulators unless explicitly instructed
- GitHub Actions and dependabot removed from fork — not needed

## Last session (2026-03-25)
- Performance optimization brainstorm (Approach 2: show-first-then-refresh)
- Implemented 6 performance optimizations: DB indexes, metadata caching, DiffUtil, RecyclerView tuning, Glide prefetch, rescan skip
- Bulk DB queries for All Media folder
- Removed Videos virtual folder
- Never show recycling bin on main page
- White selection checkmarks, hidden folders fix
- Exported baseline APK and optimized APK
- Pushed initial commit to GitHub

## Watcher test

