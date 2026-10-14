package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newAppointmentMilestone("code-83", "label-918")
        assertEquals(AppointmentMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAppointmentMilestone("code-83", "label-918")
        try {
            item.moveTo(AppointmentMilestoneStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentMilestoneStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAppointmentMilestone("code-83", "label-918")
        item.status = AppointmentMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(AppointmentMilestoneStatus.IDLE) }
    }
}
