package com.acme.transport.linehaul.backhaul.backhaulallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackBackhaulReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackBackhaulReservation("code-652", "title-619")
        assertEquals(FallbackBackhaulReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFallbackBackhaulReservation("code-652", "title-619")
        try {
            item.moveTo(FallbackBackhaulReservationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackBackhaulReservationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackBackhaulReservation("code-652", "title-619")
        item.status = FallbackBackhaulReservationStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackBackhaulReservationStatus.IDLE) }
    }
}
