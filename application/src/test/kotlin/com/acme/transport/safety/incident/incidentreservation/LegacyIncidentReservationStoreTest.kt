package com.acme.transport.safety.incident.incidentreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyIncidentReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyIncidentReservationStore()
        val item = newLegacyIncidentReservation("code-724", "title-462")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
