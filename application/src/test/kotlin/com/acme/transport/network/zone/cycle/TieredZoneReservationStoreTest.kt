package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredZoneReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredZoneReservationStore()
        val item = newTieredZoneReservation("code-197", "title-461")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
