package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newAppointmentGroup("code-592", "label-242")
        assertEquals(AppointmentGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAppointmentGroup("code-592", "label-242")
        try {
            item.moveTo(AppointmentGroupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentGroupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAppointmentGroup("code-592", "label-242")
        item.status = AppointmentGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AppointmentGroupStatus.PROPOSED) }
    }
}
