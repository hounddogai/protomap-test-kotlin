package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeSnapshotStore()
        val item = newNodeSnapshot("code-185", "label-121")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
