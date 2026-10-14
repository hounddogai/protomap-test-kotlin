package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveParcelReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveParcelReservation("code-338", "name-519")
        assertEquals(AdaptiveParcelReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAdaptiveParcelReservation("code-338", "name-519")
        try {
            item.moveTo(AdaptiveParcelReservationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveParcelReservationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveParcelReservation("code-338", "name-519")
        item.status = AdaptiveParcelReservationStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveParcelReservationStatus.DRAFT) }
    }
}
