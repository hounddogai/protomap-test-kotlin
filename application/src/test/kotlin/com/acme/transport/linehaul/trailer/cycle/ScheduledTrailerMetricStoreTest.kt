package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTrailerMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTrailerMetricStore()
        val item = newScheduledTrailerMetric("code-713", "title-928")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
