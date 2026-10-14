package com.acme.transport.linehaul.capacity.capacityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCapacityReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCapacityReservationStore()
        val item = newTieredCapacityReservation("code-821", "label-500")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
