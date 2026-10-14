package com.acme.transport.fleetops.tire.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireSlotStore()
        val item = newTireSlot("code-14", "label-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
