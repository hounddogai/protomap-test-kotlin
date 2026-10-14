package com.acme.transport.terminal.traileryard.yardreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTrailerYardReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTrailerYardReservationStore()
        val item = newSeasonalTrailerYardReservation("code-231", "label-360")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
