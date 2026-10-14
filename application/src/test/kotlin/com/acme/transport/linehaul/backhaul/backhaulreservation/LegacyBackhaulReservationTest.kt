package com.acme.transport.linehaul.backhaul.backhaulreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyBackhaulReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyBackhaulReservation("code-709", "name-978")
        assertEquals(LegacyBackhaulReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyBackhaulReservation("code-709", "name-978")
        try {
            item.moveTo(LegacyBackhaulReservationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyBackhaulReservationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyBackhaulReservation("code-709", "name-978")
        item.status = LegacyBackhaulReservationStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyBackhaulReservationStatus.IDLE) }
    }
}
