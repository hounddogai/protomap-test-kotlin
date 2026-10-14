package com.acme.transport.terminal.traileryard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTrailerYardReservationTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyTrailerYardReservation("code-433", "name-658")
        assertEquals(LegacyTrailerYardReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLegacyTrailerYardReservation("code-433", "name-658")
        try {
            item.moveTo(LegacyTrailerYardReservationStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTrailerYardReservationStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyTrailerYardReservation("code-433", "name-658")
        item.status = LegacyTrailerYardReservationStatus.FAILED
        assertFails("move") { item.moveTo(LegacyTrailerYardReservationStatus.NEW) }
    }
}
