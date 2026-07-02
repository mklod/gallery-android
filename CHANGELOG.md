# Changelog

## TODO
> [!tip] Queued for next build
> - (no items queued)

## Fork Builds (Right Gallery reskin)

## Build 2026-07-02--1647
### Changes
- Fixed deleted/moved items lingering in gallery views (root-cause fix, replaces the partial 2026-03-26 fix):
  - New `MediaTombstones` guard: paths just deleted or moved by the app are blocked (60 s) from being re-imported into the Room cache and the visible grid by overlapping background rescans. Previously any scan running during a delete/move (MediaActivity async scan, MainActivity folder loop with its pre-fetched MediaStore snapshot, `rescanFolderMedia`) would write the stale rows back with `INSERT OR REPLACE`, resurrecting the items — even overwriting the recycle-bin marker.
  - Move now rescans the **old** paths in MediaStore (a filesystem rename leaves the old rows behind, so every later scan kept re-importing the moved-away files) and updates/purges their Room rows.
  - Move to a folder outside the gallery scan scope (excluded / hidden / .nomedia) now drops the cached rows entirely instead of leaving phantom entries for All Media to show at next app start.
  - Fullscreen (ViewPager) delete/move paths now get the same treatment (they previously did no cache invalidation at all).
  - Tombstones are cleared when a file verifiably returns (recycle-bin restore, copy to the same path, MediaStore re-add), so restores are not hidden.
- Added `MediaTombstonesTest` unit tests (6 tests, all passing).

> [!warning] Testing Checklist
> - [ ] Delete photos from a folder grid (recycle bin ON) — they disappear immediately and do NOT reappear after backing out to folders and re-entering, or after pull-to-refresh
>   - Notes:
> - [ ] Same, but delete from the fullscreen viewer, then go back to the grid
>   - Notes:
> - [ ] Delete while a big scan is still running (enter All Media on a cold start and delete within the first seconds) — deleted items stay gone
>   - Notes:
> - [ ] Move photos to a folder that the gallery does not scan (hidden/.nomedia or excluded) — they vanish from source folder AND All Media, and stay gone after app restart
>   - Notes:
> - [ ] Move photos between two normal visible folders — they appear in the destination, disappear from source
>   - Notes:
> - [ ] Move a photo back to its original folder shortly after moving it out — it shows up again in the original folder
>   - Notes:
> - [ ] Recycle bin: deleted items appear in the bin; restoring puts them back and they are visible again immediately
>   - Notes:
> - [ ] Favorites survive a move between visible folders
>   - Notes:

---

## Build 2026-03-31--1419
### Changes
- Fixed pull-to-refresh spinner stuck forever when scan already in progress
- Fixed pull-to-refresh spinner styling for dark theme (dark circle, white arrow)

> [!warning] Testing Checklist
> - [ ] Pull-to-refresh shows dark spinner with white arrow
>   - Notes:
> - [ ] Pull-to-refresh dismisses promptly after data loads
>   - Notes:
> - [ ] Pull-to-refresh while already loading dismisses immediately
>   - Notes:

---

## Build 2026-03-31--0246
### Changes
- Fixed view toggle icon: now shows current view (calendar/wall/list) instead of next view
- Fixed list view broken after adapter reuse optimization (grid↔list now recreates adapter, calendar↔wall reuses)
- Fixed exclude folder not working in All Media: cached DB query now filters excluded paths
- Fixed exclude folder priority: excluding a child folder now overrides including its parent
- ~~View changer icon should show current view, not next view~~ — DONE

> [!warning] Testing Checklist
> - [x] View icon matches current view mode
>   - Notes:
> - [x] List view displays correctly (no full-width random photos)
>   - Notes:
> - [x] Exclude a folder — items disappear from All Media
>   - Notes:
> - [x] Include parent, exclude child — child stays excluded
>   - Notes:
> - [x] All perf optimizations still work (thumbnail loading, view toggle speed)
>   - Notes:

---

## Build 2026-03-31--0140
### Changes
- P0: Thumbnails decoded at grid cell size instead of full resolution (massive memory + decode savings)
- P1: Glide RecyclerViewPreloader for scroll-aware thumbnail prefetch (replaces static 30-item preload)
- P2: Adapter preserved across view toggles (no more full rebuild of 4500 items)
- P3: Parallel folder scanning (4 threads) for All Media async refresh
- P4: Room WAL mode for concurrent DB reads during writes
- Added test infrastructure: JUnit, Espresso, AndroidX Benchmark

> [!warning] Testing Checklist
> - [ ] All Media opens and shows thumbnails within 2 seconds
>   - Notes:
> - [ ] View toggle (calendar/wall/list) completes within 500ms
>   - Notes:
> - [ ] Scrolling through All Media has no visible jank
>   - Notes:
> - [ ] Regular folders load correctly
>   - Notes:
> - [ ] Pull-to-refresh works on All Media
>   - Notes:
> - [ ] Delete an item, verify it disappears
>   - Notes:
> - [ ] No "Videos" folder visible
>   - Notes:

---

## Build 2026-03-30--1846
### Changes
- Fixed blank page when switching views on large folders: recreate adapter immediately with existing mMedia data, then refresh in background
- View switching no longer nulls adapter before data is ready

> [!warning] Testing Checklist
> - [ ] View toggle (calendar→wall→list) works on All Media without blank page
>   - Notes:
> - [ ] View toggle works on Camera / large folders without blank page
>   - Notes:
> - [ ] View toggle works on small folders
>   - Notes:
> - [ ] Pull-to-refresh works (spinner shows briefly then stops)
>   - Notes:
> - [ ] No "Videos" folder visible
>   - Notes:

---

## Build 2026-03-30--1810
### Changes
- Removed rescan-skip optimization entirely (caused pull-to-refresh to break — premature optimization)
- Reverted pull-to-refresh listener to original simple `getDirectories()` call
- Added `MediaFetcher.invalidateCache()` to `refreshItems()` so file moves/copies trigger fresh data
- Stale thumbnails: rescan loop already updates tmb and DB; cache invalidation ensures metadata is fresh

### Testing Checklist
- [ ] Pull-to-refresh works on main folder view
  - Notes:
- [ ] Pull-to-refresh works on subfolder views
  - Notes:
- [ ] After moving a file out of a folder, folder thumbnail updates
  - Notes:
- [ ] After deleting a photo, gallery view reflects deletion
  - Notes:
- [ ] No "Videos" folder visible
  - Notes:

---

## Build 2026-03-30--1750
### Changes
- Fixed stale Videos folder appearing: filter SHOW_VIDEOS from dirsToShow, delete stale DB entry on startup
- Fixed pull-to-refresh on main view: reset mIsGettingDirs, invalidate caches, force fresh reload

### Testing Checklist
- [x] No "Videos" folder visible on main page
  - Notes:
- [ ] Pull-to-refresh works on main folder view
  - Notes:broken
- [ ] Pull-to-refresh still works on subfolder views
  - Notes:broke
- [ ] After deleting a photo, returning to gallery view shows it removed
  - Notes:

---

## Build 2026-03-30--1746
### Changes
- Fixed pull-to-refresh on main view: reset mIsGettingDirs, invalidate caches, and force fresh reload on pull

### Testing Checklist
- [ ] Pull-to-refresh works on main folder view
  - Notes:
- [ ] Pull-to-refresh still works on subfolder views
  - Notes:
- [ ] After deleting a photo, returning to gallery view shows it removed
  - Notes:

---

## Build 2026-03-26--2015
### Changes
- Fixed pull-to-refresh crash: rescan-skip early return now stops refresh indicator
- Fixed deleted items lingering: invalidate MediaFetcher cache after deletion
- Fixed stale folder thumbnails: reset scan IDs on resume so rescan runs fresh after returning from other activities
- Added `isDestroyed`/`isFinishing` guards on all runOnUiThread blocks in gotDirectories and gotMedia
- After deletion, immediately update adapter to remove items from UI

### Testing Checklist
- [ ] Pull-to-refresh works without crashing
  - Notes: not working on main view, fine on subfolder views
- [ ] After deleting a photo, returning to gallery view shows it removed immediately
  - Notes:
- [x] Folder thumbnail updates after deleting the thumbnail image
  - Notes:
- [x] Normal navigation still works (folders, media, detail)
  - Notes:

---

## Build 2026-03-26--2000
### Changes
- Removed Videos virtual folder entirely — only "All media" remains
- No code changes from 2026-03-25--1245, rebuild after NAS migration

### Testing Checklist
- [ ] App launches and shows folder list with "All media" at top
  - Notes:
- [ ] No "Videos" folder visible
  - Notes:
- [ ] Tapping folders loads media correctly
  - Notes:
- [ ] "All media" loads all photos/videos
  - Notes:
- [ ] Scrolling is smooth
  - Notes:

---

## Build 2026-03-25--1245
### Changes
- Removed Videos virtual folder entirely — only "All media" remains as a virtual folder
- Fixed `getCachedMedia` for SHOW_ALL: single bulk DB query (`getAllMedia()`) instead of per-folder `getFoldersToScan()` + N separate queries
- Added `getAllMedia()` and `getAllVideos()` DAO queries
- Fixed `GetMediaAsynctask` to apply `filterMedia` in showAll mode
- Removed SHOW_VIDEOS handling from MediaActivity, MainActivity, Context.kt
- Removed filterMedia reset in MainActivity.onResume

### Testing Checklist
- [ ] Main page shows "All media" folder at top, no "Videos" folder
  - Notes: 
- [ ] Tapping "All media" opens calendar view of all photos/videos
  - Notes:
- [ ] All media folder loads noticeably faster than baseline
  - Notes:
- [ ] Regular folders load and display correctly
  - Notes:
- [ ] Scrolling through thumbnails is smooth
  - Notes:
- [ ] Returning from folder/detail view back to main page is responsive
  - Notes:
- [ ] No recycling bin visible on main page
  - Notes: deletes aren't instant. they take a while to disappear from gallery view after being deleted in image detail view. after deletion, returning to gallery view, they are still visible for a few seconds before actually disappearing. and a thumbnail of folder is still the deleted image. pull down to refresh crashes up. thumbnail updates super slow post deletion, sometimes never updating. 

---

## Build 2026-03-25--0242
### Changes
- Performance: Added DB indexes on parent_path, deleted_ts, is_favorite, type (migration v10→v11)
- Performance: Cached `getLastModifieds()` and `getDateTakens()` with 30s TTL in MediaFetcher
- Performance: DiffUtil for DirectoryAdapter.updateDirs() and MediaAdapter.updateMedia()
- Performance: RecyclerView tuning — setItemViewCacheSize(20), setHasFixedSize(true)
- Performance: Glide thumbnail prefetching — preloads first 30 thumbnails on media load
- Performance: Skip redundant folder rescans when MediaStore hasn't changed

### Testing Checklist
- [ ] App starts and shows folder list
  - Notes:
- [ ] Folders load faster on second open (cached scan skip)
  - Notes:
- [ ] Scrolling through thumbnails is smooth
  - Notes:
- [ ] DB migration works (no crash on upgrade from previous version)
  - Notes:

---

## Build 2026-03-25--0231 (baseline)
### Changes
- Performance baseline APK before optimizations
- Removed `listFiles()` filesystem scan from virtual folder setup
- Config writes gated to first run only
- Reduced `applyDarkToolbarStyle` from 3 calls to 1 per onResume
- Eliminated redundant double `setupAdapter` call in MainActivity.onResume

### Testing Checklist
- [ ] App launches without crash
  - Notes:
- [ ] Basic navigation works (folders, media, detail view)
  - Notes:

---

## Build 2026-03-25--0116
### Changes
- Never show recycling bin on main page
- Fixed Videos folder thumbnail and item count (DB queries)
- Long press selection: opaque white circle with black checkmark
- "Temporarily show hidden" only reveals dot-prefixed folders, not .nomedia system folders

### Testing Checklist
- [ ] No recycle bin visible on main page
  - Notes:
- [ ] Selection checkmark is white circle / black check
  - Notes:
- [ ] "Temporarily show hidden" doesn't flood with system folders
  - Notes:

---

## Build 2026-03-24--2357
### Changes
- Three-way view toggle: calendar → wall → compact list
- Menu cleanup: removed many items from folder and main hamburger menus
- MAX_COLUMN_COUNT reduced from 20 to 6
- Thumbnail spacing set to 13
- Renamed "All" to "All media"
- Added 1px separator line above date headers in calendar view
- Added bottom padding to folder header toolbar

### Testing Checklist
- [ ] Three-way toggle cycles: calendar → wall → list
  - Notes:
- [ ] Column count options are 1-6
  - Notes:
- [ ] Date separator lines visible in calendar view
  - Notes:

---

## Upstream Changelog (Fossify Gallery)

All notable changes to the original project are documented below.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.13.1] - 2026-02-14
### Changed
- Updated translations

### Fixed
- Fixed file size calculations to use SI decimal units (divide by 1000)

## [1.13.0] - 2026-02-06
### Added
- Added "On video tap" option to choose between in-app player or default player app ([#917])

### Changed
- Updated translations

### Fixed
- Fixed unnecessary "Video has no audio" toast when looping videos ([#876])
- Fixed same pencil icons for edit and rename buttons ([#925])

## [1.12.0] - 2026-02-02
### Changed
- Videos now open in the in-app player; use the "Open with" option for other apps ([#774])
- Updated translations

### Fixed
- Fixed double-tap to zoom gesture for WebP images (again) ([#363])

## [1.11.0] - 2026-01-30
### Added
- Added support for custom fonts
- Added option to toggle Ultra HDR rendering (Android 14+) ([#564])
- Long press gesture to play videos at 2x speed in separate video player ([#830])

### Changed
- Mute button is now disabled for videos without an audio track ([#876])
- Updated translations

### Fixed
- Fixed issue with separate video player not respecting paused state when seeking ([#831])
- Fixed misplacement of extended information during slideshow ([#800])
- Fixed double-tap to zoom for WebP images ([#363])

## [1.10.0] - 2025-12-16
### Added
- Long press gesture to play videos at 2x speed ([#666])

### Changed
- Player now respects play/pause state when seeking
- Updated translations

### Fixed
- Fixed opening JXL files from other apps ([#568])

## [1.9.1] - 2025-11-25
### Changed
- Updated translations

### Fixed
- Fixed crash in editor when launched from other apps ([#786])

## [1.9.0] - 2025-11-08
### Changed
- Restored ability to show/hide notch area ([#749])

### Fixed
- Fixed overlap between editor controls and preview ([#752])
- Fixed crash when viewing photos with extended details enabled ([#754])
- Fixed cropped copies being saved in app data when setting wallpaper ([#759])
- Fixed overlap between player controls and navigation bars in landscape mode

## [1.8.1] - 2025-11-04
### Changed
- Updated translations

### Fixed
- Fixed missing resolution info in extended details for JXL images ([#659])
- Fixed Gallery not appearing when opening photos from LineageOS Camera ([#411])
- Fixed extended details showing up in full-screen in some cases ([#734])
- Fixed full-screen view not working properly on some devices ([#743])
- Fixed full-screen requiring double taps in some cases ([#734])
- Fixed overlap between bottom actions and system bar when setting wallpaper ([#747])

## [1.8.0] - 2025-10-29
### Changed
- Compatibility updates for Android 15 & 16
- Search bar is now pinned to the top when scrolling
- Updated translations

### Fixed
- Fixed overlap between extended details and bottom actions ([#418])
- Fixed loading big JXL images ([#622])
- Fixed non-functional filter in image editor ([#718])

## [1.7.0] - 2025-10-16
### Added
- Option to overwrite the original image when saving edits ([#62])

### Changed
- Updated translations

## [1.6.0] - 2025-10-01
### Added
- Added a "Force landscape (reverse)" orientation option ([#630])

### Changed
- Updated translations

### Fixed
- Fixed a glitch in pattern lock after incorrect attempts

## [1.5.2] - 2025-09-22
### Changed
- Updated translations

### Fixed
- Fixed black screen when viewing edited AVIF images ([#648])

## [1.5.1] - 2025-09-08
### Fixed
- Fixed zoom in photos ([#642])

## [1.5.0] - 2025-09-08
### Added
- Support for animated AVIF images ([#621])

### Changed
- Updated translations

### Fixed
- Fixed metadata loss (EXIF) when editing or resizing images ([#29])

## [1.4.2] - 2025-08-21
### Changed
- Updated translations

### Fixed
- Fixed media picker showing only GIFs when both images and videos are requested
- Fixed volume gesture not working on some devices ([#237])

## [1.4.1] - 2025-07-22
### Changed
- Improved seek control in videos ([#325])
- Updated translations

### Fixed
- Fixed broken looping in videos shorter than one second ([#565])
- Slideshows now automatically start in full-screen mode ([#529])
- Fixed pixelation and artifacts in some JPEG XL images ([#567])

## [1.4.0] - 2025-07-14
### Added
- Support for Ultra HDR images (Android 14+) ([#166])
- Support for wide-color-gamut images ([#375])

### Changed
- Updated translations

### Fixed
- Fixed crash in some external image editors ([#525])

## [1.3.1] - 2025-06-17
### Changed
- Updated translations

## [1.3.0] - 2025-05-31
### Added
- Copy to clipboard button for images ([#199])
- Option to keep screen on while viewing media ([#365])
- Ability to sort folders by item count ([#379])
- Confirmation dialog when restoring media ([#447])

### Changed
- Updated translations

### Fixed
- Fixed unresponsive image/video controls after rotating device ([#275])
- Swipe-to-close gesture now works with WebP images ([#362])
- Fixed inaccurate or broken seeking in some videos ([#475])
- Image rotation edits no longer auto-save without confirmation ([#241])
- External keyboards now work properly in copy/move dialogs ([#128])

## [1.2.1] - 2024-09-28
### Added
- Added option to control video playback speed
- Added option to mute videos
- Added error indicator for media load failures
- Added initial support for JPEG XL format (increased app size)

### Changed
- Updated target Android version to 14
- Replaced checkboxes with switches
- Improve scrolling performance and interface
- Improved app lock logic and interface
- Other minor bug fixes and improvements
- Added more translations

## [1.2.0] - 2024-09-21
### Added
- Added option to control video playback speed
- Added option to mute videos
- Added error indicator for media load failures
- Added initial support for JPEG XL format

### Changed
- Updated target Android version to 14
- Replaced checkboxes with switches
- Improved app lock logic and user interface
- Other minor bug fixes and improvements
- Added more translations

## [1.1.3] - 2024-04-16
### Changed
- Added some translations

### Fixed
- Fixed black thumbnails for some images.

## [1.1.2] - 2024-03-10
### Added
- Added support for AVIF.

### Changed
- Added some translations

### Fixed
- Fixed crash when playing videos.
- Fixed slideshow on Android 14.
- Fixed position reset after device rotation.
- Fixed zooming screenshots when one to one double tap zoom enabled.

## [1.1.1] - 2024-01-10
### Changed
- Added some translations

### Removed
- Removed fake app message when using the editor.

## [1.1.0] - 2024-01-02
### Changed
- Added some translations

### Removed
- Removed proprietary panorama library

## [1.0.2] - 2023-12-30
### Changed
- Added some translations

### Fixed
- Fixed zooming in high-res images

## [1.0.1] - 2023-12-28
### Changed
- Added some translation, UI/UX improvements

### Fixed
- Fixed privacy policy link

[#29]: https://github.com/FossifyOrg/Gallery/issues/29
[#62]: https://github.com/FossifyOrg/Gallery/issues/62
[#128]: https://github.com/FossifyOrg/Gallery/issues/128
[#166]: https://github.com/FossifyOrg/Gallery/issues/166
[#199]: https://github.com/FossifyOrg/Gallery/issues/199
[#237]: https://github.com/FossifyOrg/Gallery/issues/237
[#241]: https://github.com/FossifyOrg/Gallery/issues/241
[#275]: https://github.com/FossifyOrg/Gallery/issues/275
[#325]: https://github.com/FossifyOrg/Gallery/issues/325
[#362]: https://github.com/FossifyOrg/Gallery/issues/362
[#363]: https://github.com/FossifyOrg/Gallery/issues/363
[#365]: https://github.com/FossifyOrg/Gallery/issues/365
[#375]: https://github.com/FossifyOrg/Gallery/issues/375
[#379]: https://github.com/FossifyOrg/Gallery/issues/379
[#411]: https://github.com/FossifyOrg/Gallery/issues/411
[#418]: https://github.com/FossifyOrg/Gallery/issues/418
[#447]: https://github.com/FossifyOrg/Gallery/issues/447
[#475]: https://github.com/FossifyOrg/Gallery/issues/475
[#525]: https://github.com/FossifyOrg/Gallery/issues/525
[#529]: https://github.com/FossifyOrg/Gallery/issues/529
[#564]: https://github.com/FossifyOrg/Gallery/issues/564
[#565]: https://github.com/FossifyOrg/Gallery/issues/565
[#567]: https://github.com/FossifyOrg/Gallery/issues/567
[#568]: https://github.com/FossifyOrg/Gallery/issues/568
[#621]: https://github.com/FossifyOrg/Gallery/issues/621
[#622]: https://github.com/FossifyOrg/Gallery/issues/622
[#630]: https://github.com/FossifyOrg/Gallery/issues/630
[#642]: https://github.com/FossifyOrg/Gallery/issues/642
[#648]: https://github.com/FossifyOrg/Gallery/issues/648
[#659]: https://github.com/FossifyOrg/Gallery/issues/659
[#666]: https://github.com/FossifyOrg/Gallery/issues/666
[#718]: https://github.com/FossifyOrg/Gallery/issues/718
[#734]: https://github.com/FossifyOrg/Gallery/issues/734
[#743]: https://github.com/FossifyOrg/Gallery/issues/743
[#747]: https://github.com/FossifyOrg/Gallery/issues/747
[#749]: https://github.com/FossifyOrg/Gallery/issues/749
[#752]: https://github.com/FossifyOrg/Gallery/issues/752
[#754]: https://github.com/FossifyOrg/Gallery/issues/754
[#759]: https://github.com/FossifyOrg/Gallery/issues/759
[#774]: https://github.com/FossifyOrg/Gallery/issues/774
[#786]: https://github.com/FossifyOrg/Gallery/issues/786
[#800]: https://github.com/FossifyOrg/Gallery/issues/800
[#830]: https://github.com/FossifyOrg/Gallery/issues/830
[#831]: https://github.com/FossifyOrg/Gallery/issues/831
[#876]: https://github.com/FossifyOrg/Gallery/issues/876
[#917]: https://github.com/FossifyOrg/Gallery/issues/917
[#925]: https://github.com/FossifyOrg/Gallery/issues/925

[Unreleased]: https://github.com/FossifyOrg/Gallery/compare/1.13.1...HEAD
[1.13.1]: https://github.com/FossifyOrg/Gallery/compare/1.13.0...1.13.1
[1.13.0]: https://github.com/FossifyOrg/Gallery/compare/1.12.0...1.13.0
[1.12.0]: https://github.com/FossifyOrg/Gallery/compare/1.11.0...1.12.0
[1.11.0]: https://github.com/FossifyOrg/Gallery/compare/1.10.0...1.11.0
[1.10.0]: https://github.com/FossifyOrg/Gallery/compare/1.9.1...1.10.0
[1.9.1]: https://github.com/FossifyOrg/Gallery/compare/1.9.0...1.9.1
[1.9.0]: https://github.com/FossifyOrg/Gallery/compare/1.8.1...1.9.0
[1.8.1]: https://github.com/FossifyOrg/Gallery/compare/1.8.0...1.8.1
[1.8.0]: https://github.com/FossifyOrg/Gallery/compare/1.7.0...1.8.0
[1.7.0]: https://github.com/FossifyOrg/Gallery/compare/1.6.0...1.7.0
[1.6.0]: https://github.com/FossifyOrg/Gallery/compare/1.5.2...1.6.0
[1.5.2]: https://github.com/FossifyOrg/Gallery/compare/1.5.1...1.5.2
[1.5.1]: https://github.com/FossifyOrg/Gallery/compare/1.5.0...1.5.1
[1.5.0]: https://github.com/FossifyOrg/Gallery/compare/1.4.2...1.5.0
[1.4.2]: https://github.com/FossifyOrg/Gallery/compare/1.4.1...1.4.2
[1.4.1]: https://github.com/FossifyOrg/Gallery/compare/1.4.0...1.4.1
[1.4.0]: https://github.com/FossifyOrg/Gallery/compare/1.3.1...1.4.0
[1.3.1]: https://github.com/FossifyOrg/Gallery/compare/1.3.0...1.3.1
[1.3.0]: https://github.com/FossifyOrg/Gallery/compare/1.2.1...1.3.0
[1.2.1]: https://github.com/FossifyOrg/Gallery/compare/1.2.0...1.2.1
[1.2.0]: https://github.com/FossifyOrg/Gallery/compare/1.1.3...1.2.0
[1.1.3]: https://github.com/FossifyOrg/Gallery/compare/1.1.2...1.1.3
[1.1.2]: https://github.com/FossifyOrg/Gallery/compare/1.1.1...1.1.2
[1.1.1]: https://github.com/FossifyOrg/Gallery/compare/1.1.0...1.1.1
[1.1.0]: https://github.com/FossifyOrg/Gallery/compare/1.0.2...1.1.0
[1.0.2]: https://github.com/FossifyOrg/Gallery/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/FossifyOrg/Gallery/releases/tag/1.0.1

