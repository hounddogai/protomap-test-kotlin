package com.acme.transport.network.transittime.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTransitTimeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTransitTimeMetricStore()
        val item = newPrimaryTransitTimeMetric("code-421", "name-337")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
