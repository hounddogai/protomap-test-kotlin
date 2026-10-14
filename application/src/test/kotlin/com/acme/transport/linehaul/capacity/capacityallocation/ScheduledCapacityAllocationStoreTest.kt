package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledCapacityAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledCapacityAllocationStore()
        val item = newScheduledCapacityAllocation("code-709", "label-495")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
