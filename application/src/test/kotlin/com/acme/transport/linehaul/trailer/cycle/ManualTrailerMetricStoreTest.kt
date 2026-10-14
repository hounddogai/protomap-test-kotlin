package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTrailerMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTrailerMetricStore()
        val item = newManualTrailerMetric("code-627", "name-657")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
