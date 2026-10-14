package com.acme.transport.crewing.availability.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newAvailabilityReservation("code-512", "label-175")
        assertEquals(AvailabilityReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAvailabilityReservation("code-512", "label-175")
        try {
            item.moveTo(AvailabilityReservationStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityReservationStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAvailabilityReservation("code-512", "label-175")
        item.status = AvailabilityReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AvailabilityReservationStatus.PROPOSED) }
    }
}
