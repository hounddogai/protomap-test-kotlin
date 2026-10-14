package com.acme.transport.network.node.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeBaselineStore()
        val item = newNodeBaseline("code-214", "name-746")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
