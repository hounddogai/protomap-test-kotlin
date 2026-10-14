package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationReservation("code-145", "title-546")
        assertEquals(UtilizationReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newUtilizationReservation("code-145", "title-546")
        try {
            item.moveTo(UtilizationReservationStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationReservationStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationReservation("code-145", "title-546")
        item.status = UtilizationReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationReservationStatus.PLANNED) }
    }
}
