package com.acme.transport.terminal.appointment.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAppointmentMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientAppointmentMetric("code-445", "label-11")
        assertEquals(LenientAppointmentMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLenientAppointmentMetric("code-445", "label-11")
        try {
            item.moveTo(LenientAppointmentMetricStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAppointmentMetricStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientAppointmentMetric("code-445", "label-11")
        item.status = LenientAppointmentMetricStatus.STOPPED
        assertFails("move") { item.moveTo(LenientAppointmentMetricStatus.IDLE) }
    }
}
