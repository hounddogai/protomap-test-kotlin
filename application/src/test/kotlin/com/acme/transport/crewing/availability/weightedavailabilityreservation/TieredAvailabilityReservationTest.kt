package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAvailabilityReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredAvailabilityReservation("code-213", "title-182")
        assertEquals(TieredAvailabilityReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTieredAvailabilityReservation("code-213", "title-182")
        try {
            item.moveTo(TieredAvailabilityReservationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAvailabilityReservationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredAvailabilityReservation("code-213", "title-182")
        item.status = TieredAvailabilityReservationStatus.CLOSED
        assertFails("move") { item.moveTo(TieredAvailabilityReservationStatus.DRAFT) }
    }
}
