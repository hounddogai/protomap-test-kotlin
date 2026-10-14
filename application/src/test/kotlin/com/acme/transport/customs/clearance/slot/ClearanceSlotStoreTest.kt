package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceSlotStore()
        val item = newClearanceSlot("code-410", "name-69")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
