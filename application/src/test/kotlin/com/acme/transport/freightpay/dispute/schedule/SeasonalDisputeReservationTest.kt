package com.acme.transport.freightpay.dispute.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDisputeReservationTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalDisputeReservation("code-52", "label-455")
        assertEquals(SeasonalDisputeReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newSeasonalDisputeReservation("code-52", "label-455")
        try {
            item.moveTo(SeasonalDisputeReservationStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDisputeReservationStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalDisputeReservation("code-52", "label-455")
        item.status = SeasonalDisputeReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalDisputeReservationStatus.OPEN) }
    }
}
