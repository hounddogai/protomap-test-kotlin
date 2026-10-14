package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAppointmentMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryAppointmentMetric("code-962", "label-975")
        assertEquals(PrimaryAppointmentMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPrimaryAppointmentMetric("code-962", "label-975")
        try {
            item.moveTo(PrimaryAppointmentMetricStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAppointmentMetricStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryAppointmentMetric("code-962", "label-975")
        item.status = PrimaryAppointmentMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryAppointmentMetricStatus.REQUESTED) }
    }
}
