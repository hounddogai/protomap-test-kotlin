package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLanePlanReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledLanePlanReservation("code-877", "label-558")
        assertEquals(ScheduledLanePlanReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newScheduledLanePlanReservation("code-877", "label-558")
        try {
            item.moveTo(ScheduledLanePlanReservationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLanePlanReservationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledLanePlanReservation("code-877", "label-558")
        item.status = ScheduledLanePlanReservationStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledLanePlanReservationStatus.DRAFT) }
    }
}
