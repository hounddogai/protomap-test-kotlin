package com.acme.transport.terminal.traileryard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTrailerYardReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTrailerYardReservationStore()
        val item = newLegacyTrailerYardReservation("code-433", "name-658")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
