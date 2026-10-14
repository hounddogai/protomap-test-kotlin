package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newZoneReservation("code-193", "label-757")
        assertEquals(ZoneReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newZoneReservation("code-193", "label-757")
        try {
            item.moveTo(ZoneReservationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneReservationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newZoneReservation("code-193", "label-757")
        item.status = ZoneReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(ZoneReservationStatus.PLANNED) }
    }
}
