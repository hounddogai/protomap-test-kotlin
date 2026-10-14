package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentMetricTest {
    @Test
    fun `starts in new`() {
        val item = newAppointmentMetric("code-519", "label-683")
        assertEquals(AppointmentMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAppointmentMetric("code-519", "label-683")
        try {
            item.moveTo(AppointmentMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAppointmentMetric("code-519", "label-683")
        item.status = AppointmentMetricStatus.FAILED
        assertFails("move") { item.moveTo(AppointmentMetricStatus.NEW) }
    }
}
