package org.fossify.gallery

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Collections
import java.util.concurrent.Executors

class ParallelScanTest {
    @Test
    fun `concurrent list aggregation produces correct count`() {
        val results = Collections.synchronizedList(ArrayList<Int>())
        val executor = Executors.newFixedThreadPool(4)
        val folders = (1..20).toList()

        val futures = folders.map { folder ->
            executor.submit {
                val items = (1..10).map { folder * 100 + it }
                results.addAll(items)
            }
        }
        futures.forEach { it.get() }
        executor.shutdown()

        assertEquals(200, results.size)
    }
}
