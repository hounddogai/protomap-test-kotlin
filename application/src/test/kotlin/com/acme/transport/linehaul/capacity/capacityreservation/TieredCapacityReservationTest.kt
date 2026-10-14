package com.acme.transport.linehaul.capacity.capacityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCapacityReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredCapacityReservation("code-821", "label-500")
        assertEquals(TieredCapacityReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTieredCapacityReservation("code-821", "label-500")
        try {
            item.moveTo(TieredCapacityReservationStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCapacityReservationStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredCapacityReservation("code-821", "label-500")
        item.status = TieredCapacityReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredCapacityReservationStatus.PROPOSED) }
    }
}
