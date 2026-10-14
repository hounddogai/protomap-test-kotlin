package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeMetricStore()
        val item = newTransitTimeMetric("code-830", "title-585")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
