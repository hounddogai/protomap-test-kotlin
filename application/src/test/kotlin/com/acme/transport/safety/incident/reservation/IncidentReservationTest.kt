package com.acme.transport.safety.incident.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newIncidentReservation("code-673", "title-773")
        assertEquals(IncidentReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newIncidentReservation("code-673", "title-773")
        try {
            item.moveTo(IncidentReservationStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentReservationStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newIncidentReservation("code-673", "title-773")
        item.status = IncidentReservationStatus.STOPPED
        assertFails("move") { item.moveTo(IncidentReservationStatus.IDLE) }
    }
}
