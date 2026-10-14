package com.acme.transport.safety.incident.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentReservationStore()
        val item = newIncidentReservation("code-673", "title-773")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
