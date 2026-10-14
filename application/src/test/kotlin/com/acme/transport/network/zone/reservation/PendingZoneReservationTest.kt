package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingZoneReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingZoneReservation("code-631", "label-895")
        assertEquals(PendingZoneReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingZoneReservation("code-631", "label-895")
        try {
            item.moveTo(PendingZoneReservationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingZoneReservationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingZoneReservation("code-631", "label-895")
        item.status = PendingZoneReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingZoneReservationStatus.PROPOSED) }
    }
}
