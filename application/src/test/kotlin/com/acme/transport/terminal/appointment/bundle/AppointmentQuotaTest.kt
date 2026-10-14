package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentQuotaTest {
    @Test
    fun `starts in planned`() {
        val item = newAppointmentQuota("code-591", "title-909")
        assertEquals(AppointmentQuotaStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAppointmentQuota("code-591", "title-909")
        try {
            item.moveTo(AppointmentQuotaStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentQuotaStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAppointmentQuota("code-591", "title-909")
        item.status = AppointmentQuotaStatus.CANCELLED
        assertFails("move") { item.moveTo(AppointmentQuotaStatus.PLANNED) }
    }
}
