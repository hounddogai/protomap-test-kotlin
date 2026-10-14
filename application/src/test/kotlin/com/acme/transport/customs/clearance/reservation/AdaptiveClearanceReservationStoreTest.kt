package com.acme.transport.customs.clearance.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveClearanceReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveClearanceReservationStore()
        val item = newAdaptiveClearanceReservation("code-608", "label-935")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
