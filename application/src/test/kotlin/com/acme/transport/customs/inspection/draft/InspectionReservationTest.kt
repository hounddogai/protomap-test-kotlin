package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionReservation("code-373", "label-314")
        assertEquals(InspectionReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newInspectionReservation("code-373", "label-314")
        try {
            item.moveTo(InspectionReservationStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionReservationStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionReservation("code-373", "label-314")
        item.status = InspectionReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionReservationStatus.PLANNED) }
    }
}
