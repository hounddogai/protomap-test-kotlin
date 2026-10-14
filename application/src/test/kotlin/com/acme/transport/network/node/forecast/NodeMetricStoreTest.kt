package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeMetricStore()
        val item = newNodeMetric("code-320", "name-770")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
