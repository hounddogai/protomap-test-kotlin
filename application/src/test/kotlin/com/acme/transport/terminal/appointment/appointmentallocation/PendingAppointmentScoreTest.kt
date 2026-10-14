package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAppointmentScoreTest {
    @Test
    fun `starts in open`() {
        val item = newPendingAppointmentScore("code-344", "name-153")
        assertEquals(PendingAppointmentScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPendingAppointmentScore("code-344", "name-153")
        try {
            item.moveTo(PendingAppointmentScoreStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAppointmentScoreStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingAppointmentScore("code-344", "name-153")
        item.status = PendingAppointmentScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingAppointmentScoreStatus.OPEN) }
    }
}
