package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCapacityReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictCapacityReservation("code-424", "name-199")
        assertEquals(StrictCapacityReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newStrictCapacityReservation("code-424", "name-199")
        try {
            item.moveTo(StrictCapacityReservationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCapacityReservationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictCapacityReservation("code-424", "name-199")
        item.status = StrictCapacityReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictCapacityReservationStatus.PLANNED) }
    }
}
