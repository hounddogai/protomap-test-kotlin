package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTireReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyTireReservation("code-401", "name-273")
        assertEquals(LegacyTireReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacyTireReservation("code-401", "name-273")
        try {
            item.moveTo(LegacyTireReservationStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTireReservationStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyTireReservation("code-401", "name-273")
        item.status = LegacyTireReservationStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyTireReservationStatus.IDLE) }
    }
}
