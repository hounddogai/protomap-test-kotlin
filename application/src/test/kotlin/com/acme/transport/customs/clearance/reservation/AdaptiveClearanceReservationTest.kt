package com.acme.transport.customs.clearance.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveClearanceReservationTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveClearanceReservation("code-608", "label-935")
        assertEquals(AdaptiveClearanceReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAdaptiveClearanceReservation("code-608", "label-935")
        try {
            item.moveTo(AdaptiveClearanceReservationStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveClearanceReservationStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveClearanceReservation("code-608", "label-935")
        item.status = AdaptiveClearanceReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveClearanceReservationStatus.OPEN) }
    }
}
