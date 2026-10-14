package com.acme.transport.network.node.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackNodeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackNodeMetricStore()
        val item = newFallbackNodeMetric("code-722", "title-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
