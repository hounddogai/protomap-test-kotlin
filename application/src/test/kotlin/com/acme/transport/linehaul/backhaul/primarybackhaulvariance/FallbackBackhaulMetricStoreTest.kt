package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackBackhaulMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackBackhaulMetricStore()
        val item = newFallbackBackhaulMetric("code-116", "label-741")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
