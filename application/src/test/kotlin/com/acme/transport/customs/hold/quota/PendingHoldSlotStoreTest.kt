package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingHoldSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingHoldSlotStore()
        val item = newPendingHoldSlot("code-458", "title-800")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
