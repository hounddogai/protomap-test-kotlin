package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldSlotStore()
        val item = newHoldSlot("code-997", "name-712")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
