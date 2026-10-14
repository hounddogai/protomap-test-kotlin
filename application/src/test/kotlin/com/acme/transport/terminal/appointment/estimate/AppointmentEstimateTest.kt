package com.acme.transport.terminal.appointment.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newAppointmentEstimate("code-121", "label-519")
        assertEquals(AppointmentEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAppointmentEstimate("code-121", "label-519")
        try {
            item.moveTo(AppointmentEstimateStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentEstimateStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAppointmentEstimate("code-121", "label-519")
        item.status = AppointmentEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(AppointmentEstimateStatus.IDLE) }
    }
}
