// Last modified: 2026-07-02--1635
package org.fossify.gallery.helpers

import java.util.concurrent.ConcurrentHashMap

/**
 * Guards the media cache against resurrection of deleted/moved files.
 *
 * Background rescans read MediaStore (which lags behind the filesystem after an
 * in-app delete or rename) or operate on point-in-time snapshots taken before the
 * operation, then write their results back into the Room cache with REPLACE and
 * into the visible grid. Without a guard, any scan overlapping a delete/move
 * re-imports the dead rows and the items reappear.
 *
 * Paths registered here must be filtered out of every scan result before it is
 * shown or written to the media table. Tombstones expire after [TTL_MS] (by then
 * MediaStore has been rescanned and no longer reports the path) and are cleared
 * explicitly when a file verifiably reappears (recycle bin restore, new file at
 * the same path).
 */
object MediaTombstones {
    const val TTL_MS = 60_000L

    // lowercased full path -> expiry timestamp; paths compare case-insensitively (COLLATE NOCASE)
    private val tombstones = ConcurrentHashMap<String, Long>()

    fun add(path: String, nowMs: Long = System.currentTimeMillis()) {
        tombstones[path.lowercase()] = nowMs + TTL_MS
    }

    fun addAll(paths: Collection<String>, nowMs: Long = System.currentTimeMillis()) {
        val expiry = nowMs + TTL_MS
        paths.forEach { tombstones[it.lowercase()] = expiry }
    }

    fun clear(path: String) {
        tombstones.remove(path.lowercase())
    }

    fun isTombstoned(path: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        val key = path.lowercase()
        val expiry = tombstones[key] ?: return false
        if (expiry <= nowMs) {
            tombstones.remove(key)
            return false
        }
        return true
    }

    fun isEmpty() = tombstones.isEmpty()

    fun reset() {
        tombstones.clear()
    }
}
