package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedServicePlanReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedServicePlanReservationStore()
        val item = newBatchedServicePlanReservation("code-216", "title-356")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
