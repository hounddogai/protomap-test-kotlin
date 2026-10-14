package com.acme.transport.fleetops.defect.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectSlotStore()
        val item = newDefectSlot("code-11", "label-937")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
