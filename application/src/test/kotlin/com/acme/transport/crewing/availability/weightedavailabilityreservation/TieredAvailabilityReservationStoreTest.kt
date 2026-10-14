package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredAvailabilityReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredAvailabilityReservationStore()
        val item = newTieredAvailabilityReservation("code-213", "title-182")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
