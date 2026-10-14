package com.acme.transport.crewing.availability.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityReservationStore()
        val item = newAvailabilityReservation("code-512", "label-175")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
