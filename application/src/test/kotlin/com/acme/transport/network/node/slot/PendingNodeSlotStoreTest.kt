package com.acme.transport.network.node.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingNodeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingNodeSlotStore()
        val item = newPendingNodeSlot("code-115", "title-425")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
