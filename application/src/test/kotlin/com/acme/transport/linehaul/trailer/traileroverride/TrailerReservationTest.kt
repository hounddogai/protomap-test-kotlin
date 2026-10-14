package com.acme.transport.linehaul.trailer.traileroverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newTrailerReservation("code-780", "name-738")
        assertEquals(TrailerReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTrailerReservation("code-780", "name-738")
        try {
            item.moveTo(TrailerReservationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerReservationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTrailerReservation("code-780", "name-738")
        item.status = TrailerReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TrailerReservationStatus.PROPOSED) }
    }
}
