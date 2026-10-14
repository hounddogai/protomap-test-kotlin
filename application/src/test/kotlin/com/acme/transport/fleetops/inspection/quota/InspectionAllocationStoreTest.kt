package com.acme.transport.fleetops.inspection.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionAllocationStore()
        val item = newInspectionAllocation("code-872", "label-884")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
