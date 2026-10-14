package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliveryReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalRedeliveryReservation("code-554", "title-470")
        assertEquals(SeasonalRedeliveryReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newSeasonalRedeliveryReservation("code-554", "title-470")
        try {
            item.moveTo(SeasonalRedeliveryReservationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliveryReservationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalRedeliveryReservation("code-554", "title-470")
        item.status = SeasonalRedeliveryReservationStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalRedeliveryReservationStatus.DRAFT) }
    }
}
