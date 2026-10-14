package com.acme.transport.emissions.evcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeSlotStore()
        val item = newEvChargeSlot("code-15", "name-192")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
