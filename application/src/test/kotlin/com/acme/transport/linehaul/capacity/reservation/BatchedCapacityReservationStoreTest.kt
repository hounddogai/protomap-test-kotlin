package com.acme.transport.linehaul.capacity.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCapacityReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCapacityReservationStore()
        val item = newBatchedCapacityReservation("code-229", "title-807")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
