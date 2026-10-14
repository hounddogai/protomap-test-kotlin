package com.acme.transport.network.node.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeSlotStore()
        val item = newNodeSlot("code-680", "label-824")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
