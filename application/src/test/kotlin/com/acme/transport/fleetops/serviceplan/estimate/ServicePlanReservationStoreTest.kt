package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanReservationStore()
        val item = newServicePlanReservation("code-794", "name-655")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
