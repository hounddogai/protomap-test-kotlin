package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredZoneReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredZoneReservation("code-197", "title-461")
        assertEquals(TieredZoneReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredZoneReservation("code-197", "title-461")
        try {
            item.moveTo(TieredZoneReservationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredZoneReservationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredZoneReservation("code-197", "title-461")
        item.status = TieredZoneReservationStatus.STOPPED
        assertFails("move") { item.moveTo(TieredZoneReservationStatus.IDLE) }
    }
}
