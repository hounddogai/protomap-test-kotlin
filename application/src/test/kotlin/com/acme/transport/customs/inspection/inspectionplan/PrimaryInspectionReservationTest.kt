package com.acme.transport.customs.inspection.inspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionReservationTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryInspectionReservation("code-182", "label-364")
        assertEquals(PrimaryInspectionReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPrimaryInspectionReservation("code-182", "label-364")
        try {
            item.moveTo(PrimaryInspectionReservationStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionReservationStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryInspectionReservation("code-182", "label-364")
        item.status = PrimaryInspectionReservationStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryInspectionReservationStatus.NEW) }
    }
}
