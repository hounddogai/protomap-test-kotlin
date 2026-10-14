package com.acme.transport.safety.incident.incidentreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIncidentReservationTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyIncidentReservation("code-724", "title-462")
        assertEquals(LegacyIncidentReservationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacyIncidentReservation("code-724", "title-462")
        try {
            item.moveTo(LegacyIncidentReservationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIncidentReservationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyIncidentReservation("code-724", "title-462")
        item.status = LegacyIncidentReservationStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyIncidentReservationStatus.CREATED) }
    }
}
