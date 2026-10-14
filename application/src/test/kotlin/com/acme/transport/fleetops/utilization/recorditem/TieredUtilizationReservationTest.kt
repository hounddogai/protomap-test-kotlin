package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredUtilizationReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredUtilizationReservation("code-901", "title-993")
        assertEquals(TieredUtilizationReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredUtilizationReservation("code-901", "title-993")
        try {
            item.moveTo(TieredUtilizationReservationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredUtilizationReservationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredUtilizationReservation("code-901", "title-993")
        item.status = TieredUtilizationReservationStatus.STOPPED
        assertFails("move") { item.moveTo(TieredUtilizationReservationStatus.IDLE) }
    }
}
