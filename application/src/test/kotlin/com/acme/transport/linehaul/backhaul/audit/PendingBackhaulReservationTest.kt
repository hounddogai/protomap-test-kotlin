package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBackhaulReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingBackhaulReservation("code-252", "name-263")
        assertEquals(PendingBackhaulReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newPendingBackhaulReservation("code-252", "name-263")
        try {
            item.moveTo(PendingBackhaulReservationStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBackhaulReservationStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingBackhaulReservation("code-252", "name-263")
        item.status = PendingBackhaulReservationStatus.STOPPED
        assertFails("move") { item.moveTo(PendingBackhaulReservationStatus.IDLE) }
    }
}
