package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAppointmentVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledAppointmentVariance("code-140", "title-419")
        assertEquals(ScheduledAppointmentVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newScheduledAppointmentVariance("code-140", "title-419")
        try {
            item.moveTo(ScheduledAppointmentVarianceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAppointmentVarianceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledAppointmentVariance("code-140", "title-419")
        item.status = ScheduledAppointmentVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledAppointmentVarianceStatus.OPEN) }
    }
}
