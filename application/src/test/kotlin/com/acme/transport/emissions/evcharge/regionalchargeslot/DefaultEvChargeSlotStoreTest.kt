package com.acme.transport.emissions.evcharge.regionalchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultEvChargeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultEvChargeSlotStore()
        val item = newDefaultEvChargeSlot("code-567", "title-7")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
