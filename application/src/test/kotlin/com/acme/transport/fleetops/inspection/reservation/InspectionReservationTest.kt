package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionReservation("code-61", "name-487")
        assertEquals(InspectionReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newInspectionReservation("code-61", "name-487")
        try {
            item.moveTo(InspectionReservationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionReservationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionReservation("code-61", "name-487")
        item.status = InspectionReservationStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionReservationStatus.DRAFT) }
    }
}
