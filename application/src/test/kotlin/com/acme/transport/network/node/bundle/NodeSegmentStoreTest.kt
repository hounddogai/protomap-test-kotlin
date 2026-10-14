package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeSegmentStore()
        val item = newNodeSegment("code-538", "label-294")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
