package com.acme.transport.fleetops.defect.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDefectAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDefectAllocationStore()
        val item = newRegionalDefectAllocation("code-296", "name-418")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
