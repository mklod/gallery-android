// Last modified: 2026-07-02--1635
package org.fossify.gallery

import org.fossify.gallery.helpers.MediaTombstones
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MediaTombstonesTest {

    @Before
    fun setUp() {
        MediaTombstones.reset()
    }

    @Test
    fun `deleted path is tombstoned`() {
        MediaTombstones.add("/storage/emulated/0/DCIM/Camera/IMG_001.jpg")
        assertTrue(MediaTombstones.isTombstoned("/storage/emulated/0/DCIM/Camera/IMG_001.jpg"))
        assertFalse(MediaTombstones.isTombstoned("/storage/emulated/0/DCIM/Camera/IMG_002.jpg"))
    }

    @Test
    fun `lookup is case insensitive like the media db`() {
        // Room queries use COLLATE NOCASE; MediaStore paths can differ in case
        MediaTombstones.add("/storage/emulated/0/DCIM/Camera/IMG_001.jpg")
        assertTrue(MediaTombstones.isTombstoned("/storage/emulated/0/dcim/camera/img_001.JPG"))
    }

    @Test
    fun `tombstone expires after ttl`() {
        val now = 1_000_000L
        MediaTombstones.add("/a/b.jpg", nowMs = now)
        assertTrue(MediaTombstones.isTombstoned("/a/b.jpg", nowMs = now + MediaTombstones.TTL_MS - 1))
        assertFalse(MediaTombstones.isTombstoned("/a/b.jpg", nowMs = now + MediaTombstones.TTL_MS + 1))
    }

    @Test
    fun `clear removes tombstone immediately`() {
        // e.g. file restored from the recycle bin to its original path
        MediaTombstones.add("/a/b.jpg")
        MediaTombstones.clear("/A/B.JPG")
        assertFalse(MediaTombstones.isTombstoned("/a/b.jpg"))
    }

    @Test
    fun `addAll tombstones every path`() {
        MediaTombstones.addAll(listOf("/a/1.jpg", "/a/2.jpg"))
        assertTrue(MediaTombstones.isTombstoned("/a/1.jpg"))
        assertTrue(MediaTombstones.isTombstoned("/a/2.jpg"))
    }

    @Test
    fun `re-adding after expiry works`() {
        val now = 1_000_000L
        MediaTombstones.add("/a/b.jpg", nowMs = now)
        val later = now + MediaTombstones.TTL_MS + 1
        assertFalse(MediaTombstones.isTombstoned("/a/b.jpg", nowMs = later))
        MediaTombstones.add("/a/b.jpg", nowMs = later)
        assertTrue(MediaTombstones.isTombstoned("/a/b.jpg", nowMs = later + 1))
    }
}
