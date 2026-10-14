package com.acme.transport.crewing.availability.availabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedAvailabilityReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedAvailabilityReservationStore()
        val item = newCappedAvailabilityReservation("code-965", "name-64")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
