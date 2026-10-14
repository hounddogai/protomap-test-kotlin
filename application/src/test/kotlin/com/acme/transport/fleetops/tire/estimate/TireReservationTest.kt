package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newTireReservation("code-547", "title-627")
        assertEquals(TireReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTireReservation("code-547", "title-627")
        try {
            item.moveTo(TireReservationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireReservationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTireReservation("code-547", "title-627")
        item.status = TireReservationStatus.CLOSED
        assertFails("move") { item.moveTo(TireReservationStatus.DRAFT) }
    }
}
