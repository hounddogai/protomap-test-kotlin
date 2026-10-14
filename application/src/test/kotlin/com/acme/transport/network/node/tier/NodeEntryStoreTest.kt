package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeEntryStore()
        val item = newNodeEntry("code-185", "name-905")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
