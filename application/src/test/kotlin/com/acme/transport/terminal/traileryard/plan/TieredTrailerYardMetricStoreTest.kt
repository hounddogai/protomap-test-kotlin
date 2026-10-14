package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTrailerYardMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTrailerYardMetricStore()
        val item = newTieredTrailerYardMetric("code-307", "label-530")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
