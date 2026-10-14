package com.acme.transport.fleetops.defect.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectAllocationStore()
        val item = newDefectAllocation("code-904", "label-25")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
