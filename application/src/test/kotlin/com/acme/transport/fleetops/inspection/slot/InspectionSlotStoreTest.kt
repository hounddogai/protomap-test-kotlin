package com.acme.transport.fleetops.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionSlotStore()
        val item = newInspectionSlot("code-394", "name-645")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
