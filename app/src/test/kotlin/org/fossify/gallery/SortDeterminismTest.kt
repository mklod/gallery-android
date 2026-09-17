// Last modified: 2026-09-17--1603
package org.fossify.gallery

import org.fossify.commons.helpers.SORT_BY_DATE_MODIFIED
import org.fossify.commons.helpers.SORT_DESCENDING
import org.fossify.gallery.helpers.MediaFetcher
import org.fossify.gallery.helpers.TYPE_IMAGES
import org.fossify.gallery.models.Medium
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SortDeterminismTest {

    private fun medium(path: String, modified: Long) = Medium(
        id = null,
        name = path.substringAfterLast('/'),
        path = path,
        parentPath = path.substringBeforeLast('/'),
        modified = modified,
        taken = modified,
        size = 0L,
        type = TYPE_IMAGES,
        videoDuration = 0,
        isFavorite = false,
        deletedTS = 0L,
        mediaStoreId = 0L
    )

    @Test
    fun `tied sort keys order deterministically regardless of input order`() {
        // second-granularity timestamps tie constantly; the parallel scan feeds folders in
        // non-deterministic order, so without a tie-break every refresh looked like a reorder
        val fetcher = MediaFetcher(RuntimeEnvironment.getApplication())
        val sorting = SORT_BY_DATE_MODIFIED or SORT_DESCENDING

        val paths = listOf("/x/c.jpg", "/x/a.jpg", "/y/d.jpg", "/x/b.jpg")
        val listA = ArrayList(paths.map { medium(it, 1_000_000L) })
        val listB = ArrayList(paths.reversed().map { medium(it, 1_000_000L) })

        fetcher.sortMedia(listA, sorting)
        fetcher.sortMedia(listB, sorting)

        assertEquals(listA.map { it.path }, listB.map { it.path })
        assertEquals(listA.hashCode(), listB.hashCode())
    }

    @Test
    fun `primary sort key still wins over the tie-break`() {
        val fetcher = MediaFetcher(RuntimeEnvironment.getApplication())
        val sorting = SORT_BY_DATE_MODIFIED or SORT_DESCENDING

        val list = arrayListOf(
            medium("/x/a.jpg", 1_000L),
            medium("/x/z.jpg", 3_000L),
            medium("/x/m.jpg", 2_000L)
        )
        fetcher.sortMedia(list, sorting)

        assertEquals(listOf("/x/z.jpg", "/x/m.jpg", "/x/a.jpg"), list.map { it.path })
    }
}
