package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRosterReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRosterReservationStore()
        val item = newStrictRosterReservation("code-625", "name-303")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
