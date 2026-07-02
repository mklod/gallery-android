# Fossify Gallery Reskin — WORKPLAN

## Project Summary
Aesthetic reskin of [Fossify Gallery v1.13.1](https://github.com/FossifyOrg/Gallery) (open-source Android gallery app).
Goal: make the app look and feel more like Google Photos / "Right Gallery" in dark mode — clean, modern, minimal.

## Tech Stack
- **Language:** Kotlin
- **Platform:** Android (Gradle Kotlin DSL build)
- **Key libs:** Glide, Picasso, ExoPlayer, Room, AndroidX
- **Source location:** `Gallery-1.13.1/`

## Key Files Modified

| File | Changes |
|------|---------|
| `app/src/main/res/layout/activity_main.xml` | Replaced MySearchMenu with MaterialToolbar, black BG, RecyclerView padding |
| `app/src/main/res/layout/activity_media.xml` | Black BG, added folder title header (Apple-style font) |
| `app/src/main/res/layout/directory_item_grid_rounded_corners.xml` | Updated padding/margins for tile spacing |
| `app/src/main/res/layout/directory_item_grid_square.xml` | Updated padding from 1px to 6dp |
| `app/src/main/res/layout/directory_item_list.xml` | Restyled to Google Photos list style (no thumbnail, dividers, clean typography) |
| `app/src/main/res/layout/thumbnail_section.xml` | Apple-style date section headers |
| `app/src/main/res/drawable/placeholder_rounded_big.xml` | 18dp corner radius |
| `app/src/main/res/drawable/ic_view_grid_vector.xml` | **NEW** — grid view toggle icon |
| `app/src/main/res/drawable/ic_view_list_vector.xml` | **NEW** — list view toggle icon |
| `app/src/main/res/values/dimens.xml` | Added dir_grid_padding (6dp), dir_grid_corner_radius (18dp) |
| `app/src/main/res/menu/menu_main.xml` | Removed camera button, removed change_view_type overflow item, added toggle_view icon |
| `app/src/main/res/menu/menu_viewpager.xml` | Removed info/properties button |
| `app/src/main/kotlin/.../activities/MainActivity.kt` | Removed search bar, replaced with MaterialToolbar, added toggle view type icon, removed camera handler |
| `app/src/main/kotlin/.../activities/ViewPagerActivity.kt` | Removed properties handler |
| `app/src/main/kotlin/.../activities/MediaActivity.kt` | Added folder title header display |
| `app/src/main/kotlin/.../extensions/Context.kt` | Updated corner radius refs to 18dp for Glide/Picasso |
| `app/src/main/kotlin/.../helpers/Config.kt` | Default folder grouping changed to GROUP_BY_DATE_TAKEN_DAILY |
| `app/src/main/kotlin/.../dialogs/ChangeFolderThumbnailStyleDialog.kt` | Updated corner radius ref |

## Stages

### Stage 1: Folder Grid Reskin — COMPLETE
- [x] Black background on main activity
- [x] Increased padding between folder tiles (6dp)
- [x] Rounded corners on thumbnails (18dp)
- [x] Updated placeholder and Glide/Picasso corner radius

### Stage 2: Remove Unnecessary UI Elements — COMPLETE
- [x] Removed camera icon button from folders view toolbar
- [x] Removed info/properties button from gallery viewer toolbar

### Stage 3: Date-Grouped Folder Contents — COMPLETE
- [x] Changed default folder grouping to GROUP_BY_DATE_TAKEN_DAILY (descending)
- [x] Added folder title header (Apple-style: sans-serif-light 28sp, white, letter-spaced)
- [x] Restyled date section headers (sans-serif-medium, generous padding)
- [x] Black background on media activity

### Stage 4: Search Bar Removal & View Toggle — COMPLETE
- [x] Replaced MySearchMenu with MaterialToolbar (removes search bar from home)
- [x] Added toggle_view icon to toolbar (switches grid ↔ list instantly)
- [x] Icon updates dynamically (shows list icon when in grid, grid icon when in list)
- [x] Restyled directory_item_list.xml to Google Photos aesthetic (clean rows, dividers, no thumbnails, 16sp font)
- [x] Removed old dialog-based changeViewType flow
- [x] Created ic_view_grid_vector.xml and ic_view_list_vector.xml drawables

### Stage 5: Further Polish — COMPLETE
- [x] Virtual "All media" folder at top of main page
- [x] Three-way view toggle: calendar → wall → list
- [x] Menu cleanup (removed clutter from folder/main hamburger menus)
- [x] Custom app icon (dark bg, white landscape icon)
- [x] Opaque black header/footer in detail view
- [x] White selection checkmarks, white text everywhere
- [x] Removed recycle bin from main page
- [x] "Temporarily show hidden" only shows dot-prefixed folders

### Stage 6: Performance Optimization — IN PROGRESS

#### Completed
- [x] DB indexes on parent_path, deleted_ts, is_favorite, type (migration v10→v11)
- [x] Cached `getLastModifieds()` / `getDateTakens()` with 30s TTL
- [x] DiffUtil for adapter updates (replaces notifyDataSetChanged)
- [x] RecyclerView tuning (setItemViewCacheSize(20), setHasFixedSize(true))
- [x] Glide thumbnail prefetching (first 30 items on media load)
- [x] Skip redundant folder rescans when MediaStore unchanged
- [x] Bulk DB queries for "All media" (getAllMedia() instead of per-folder scan)
- [x] Removed Videos virtual folder (eliminated complexity)

#### Implemented (Build 2026-03-31--0140)
- [x] Thumbnail size optimization — `.override(gridCellSize)` for grid-cell-sized decode
- [x] Glide RecyclerView integration — `RecyclerViewPreloader` with 15-item lookahead
- [x] Reduce adapter recreation — adapter preserved across view toggles via `updateViewType()`
- [x] Parallel folder scanning — 4-thread `ExecutorService` in `GetMediaAsynctask`
- [x] Room WAL mode — `setJournalMode(WRITE_AHEAD_LOGGING)` on Room builder
- [x] Test infrastructure — JUnit 4, Espresso, AndroidX Benchmark, benchmark-loop.sh

#### Dev path notes (Build 2026-03-31--0246)
Adapter reuse (P2) needed revision: grid↔list toggle requires adapter recreation because `onCreateViewHolder` inflates completely different layouts (PhotoItemGridBinding vs PhotoItemListBinding). ViewHolders from the recycled pool have the wrong layout. Final approach: **reuse adapter for calendar↔wall** (both grid), **recreate for grid↔list**. This still eliminates the biggest cost (calendar↔wall was the most common toggle with 4500 items in All Media).

Exclude folder bug was pre-existing in upstream Fossify: `shouldFolderBeVisible()` in String.kt checked `isThisOrParentIncluded` before `isThisOrParentExcluded`. If DCIM was included and TestPhotos excluded, the include won. Swapped the order so excludes take priority over parent includes. Also added exclude filtering to `getCachedMedia()` for the SHOW_ALL bulk DB query path, which had no exclude logic at all.

#### Bugfixes (Build 2026-03-31--0246)
- [x] View toggle icon shows current view instead of next view
- [x] List view fixed (adapter recreated on grid↔list, reused on calendar↔wall)
- [x] Exclude folder in All Media cached query (`getCachedMedia` now filters `excludedFolders`)
- [x] Exclude priority fix (`isThisOrParentExcluded` checked before `isThisOrParentIncluded`)

#### Brainstorm: Remaining Optimizations
1. **Lazy MediaStore refresh** — Show DB cache instantly, refresh from MediaStore only when app is idle or user pull-to-refreshes.
2. **In-memory LRU directory cache** — Keep the last N directory listings in RAM so navigating back doesn't re-query Room at all.
3. **Eliminate unnecessary clones** — Multiple `.clone()` calls on media/directory lists throughout the codebase.
4. **Profile-guided optimization (PGO)** — Enable baseline profiles for the APK so critical code paths are pre-compiled on install.
5. **Startup tracing** — Use `androidx.tracing` + Perfetto to identify actual slowest operations during cold start.
