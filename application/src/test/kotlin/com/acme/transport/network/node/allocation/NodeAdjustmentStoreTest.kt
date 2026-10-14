package com.acme.transport.network.node.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeAdjustmentStore()
        val item = newNodeAdjustment("code-740", "title-768")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
