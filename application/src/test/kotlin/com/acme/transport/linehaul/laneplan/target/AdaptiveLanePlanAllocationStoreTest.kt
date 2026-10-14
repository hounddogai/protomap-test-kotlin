package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLanePlanAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLanePlanAllocationStore()
        val item = newAdaptiveLanePlanAllocation("code-25", "title-757")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
