package com.acme.transport.fleetops.defect.defectmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDefectSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDefectSlotStore()
        val item = newPendingDefectSlot("code-178", "label-177")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
