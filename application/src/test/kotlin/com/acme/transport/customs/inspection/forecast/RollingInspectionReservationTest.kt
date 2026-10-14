package com.acme.transport.customs.inspection.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingInspectionReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingInspectionReservation("code-846", "name-827")
        assertEquals(RollingInspectionReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRollingInspectionReservation("code-846", "name-827")
        try {
            item.moveTo(RollingInspectionReservationStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingInspectionReservationStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingInspectionReservation("code-846", "name-827")
        item.status = RollingInspectionReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingInspectionReservationStatus.PROPOSED) }
    }
}
