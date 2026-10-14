package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveZoneReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveZoneReservation("code-199", "label-402")
        assertEquals(AdaptiveZoneReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAdaptiveZoneReservation("code-199", "label-402")
        try {
            item.moveTo(AdaptiveZoneReservationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveZoneReservationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveZoneReservation("code-199", "label-402")
        item.status = AdaptiveZoneReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveZoneReservationStatus.PLANNED) }
    }
}
