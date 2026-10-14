package com.acme.transport.terminal.traileryard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerYardReservation("code-287", "title-40")
        assertEquals(TrailerYardReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTrailerYardReservation("code-287", "title-40")
        try {
            item.moveTo(TrailerYardReservationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardReservationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerYardReservation("code-287", "title-40")
        item.status = TrailerYardReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerYardReservationStatus.REQUESTED) }
    }
}
