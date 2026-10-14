package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCapacityScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCapacityScheduleStore()
        val item = newBatchedCapacitySchedule("code-769", "title-502")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
