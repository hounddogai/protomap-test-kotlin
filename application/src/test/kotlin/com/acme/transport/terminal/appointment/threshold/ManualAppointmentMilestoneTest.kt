package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAppointmentMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newManualAppointmentMilestone("code-671", "title-549")
        assertEquals(ManualAppointmentMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newManualAppointmentMilestone("code-671", "title-549")
        try {
            item.moveTo(ManualAppointmentMilestoneStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAppointmentMilestoneStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualAppointmentMilestone("code-671", "title-549")
        item.status = ManualAppointmentMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualAppointmentMilestoneStatus.PLANNED) }
    }
}
