package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTireReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTireReservationStore()
        val item = newLegacyTireReservation("code-401", "name-273")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
