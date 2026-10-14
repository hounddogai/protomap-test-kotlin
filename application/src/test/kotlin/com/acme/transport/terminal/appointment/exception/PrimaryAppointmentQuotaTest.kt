package com.acme.transport.terminal.appointment.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAppointmentQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryAppointmentQuota("code-348", "name-507")
        assertEquals(PrimaryAppointmentQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPrimaryAppointmentQuota("code-348", "name-507")
        try {
            item.moveTo(PrimaryAppointmentQuotaStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAppointmentQuotaStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryAppointmentQuota("code-348", "name-507")
        item.status = PrimaryAppointmentQuotaStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryAppointmentQuotaStatus.NEW) }
    }
}
