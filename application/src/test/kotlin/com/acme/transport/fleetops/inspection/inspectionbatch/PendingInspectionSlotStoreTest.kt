package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingInspectionSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingInspectionSlotStore()
        val item = newPendingInspectionSlot("code-140", "name-738")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
