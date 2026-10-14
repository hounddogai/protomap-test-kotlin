package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeReservationStore()
        val item = newEvChargeReservation("code-472", "label-137")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
