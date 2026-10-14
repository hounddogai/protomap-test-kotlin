package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireReservationStore()
        val item = newTireReservation("code-547", "title-627")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
