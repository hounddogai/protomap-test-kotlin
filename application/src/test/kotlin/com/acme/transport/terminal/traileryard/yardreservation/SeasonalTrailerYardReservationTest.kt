package com.acme.transport.terminal.traileryard.yardreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTrailerYardReservationTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalTrailerYardReservation("code-231", "label-360")
        assertEquals(SeasonalTrailerYardReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalTrailerYardReservation("code-231", "label-360")
        try {
            item.moveTo(SeasonalTrailerYardReservationStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTrailerYardReservationStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalTrailerYardReservation("code-231", "label-360")
        item.status = SeasonalTrailerYardReservationStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalTrailerYardReservationStatus.NEW) }
    }
}
