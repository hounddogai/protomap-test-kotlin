package com.acme.transport.linehaul.backhaul.backhaulallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackBackhaulReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackBackhaulReservationStore()
        val item = newFallbackBackhaulReservation("code-652", "title-619")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
