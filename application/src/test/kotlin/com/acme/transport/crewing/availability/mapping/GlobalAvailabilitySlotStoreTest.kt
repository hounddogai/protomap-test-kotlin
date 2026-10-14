package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalAvailabilitySlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalAvailabilitySlotStore()
        val item = newGlobalAvailabilitySlot("code-185", "name-364")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
