package com.acme.transport.terminal.appointment.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAppointmentBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newPendingAppointmentBaseline("code-11", "name-402")
        assertEquals(PendingAppointmentBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPendingAppointmentBaseline("code-11", "name-402")
        try {
            item.moveTo(PendingAppointmentBaselineStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAppointmentBaselineStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingAppointmentBaseline("code-11", "name-402")
        item.status = PendingAppointmentBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingAppointmentBaselineStatus.OPEN) }
    }
}
