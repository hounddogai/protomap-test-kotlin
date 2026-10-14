package com.acme.transport.fleetops.inspection.inspectionslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientInspectionSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientInspectionSlotStore()
        val item = newLenientInspectionSlot("code-789", "label-951")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
