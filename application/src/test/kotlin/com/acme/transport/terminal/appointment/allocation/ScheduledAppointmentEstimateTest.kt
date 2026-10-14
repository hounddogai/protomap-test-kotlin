package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAppointmentEstimateTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledAppointmentEstimate("code-524", "name-981")
        assertEquals(ScheduledAppointmentEstimateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledAppointmentEstimate("code-524", "name-981")
        try {
            item.moveTo(ScheduledAppointmentEstimateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAppointmentEstimateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledAppointmentEstimate("code-524", "name-981")
        item.status = ScheduledAppointmentEstimateStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledAppointmentEstimateStatus.DRAFT) }
    }
}
