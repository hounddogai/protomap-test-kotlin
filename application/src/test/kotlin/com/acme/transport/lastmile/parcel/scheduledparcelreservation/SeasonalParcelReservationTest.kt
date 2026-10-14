package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalParcelReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalParcelReservation("code-97", "label-795")
        assertEquals(SeasonalParcelReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSeasonalParcelReservation("code-97", "label-795")
        try {
            item.moveTo(SeasonalParcelReservationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalParcelReservationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalParcelReservation("code-97", "label-795")
        item.status = SeasonalParcelReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalParcelReservationStatus.PLANNED) }
    }
}
