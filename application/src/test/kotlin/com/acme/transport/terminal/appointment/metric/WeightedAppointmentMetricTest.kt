package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAppointmentMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedAppointmentMetric("code-422", "name-929")
        assertEquals(WeightedAppointmentMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newWeightedAppointmentMetric("code-422", "name-929")
        try {
            item.moveTo(WeightedAppointmentMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAppointmentMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedAppointmentMetric("code-422", "name-929")
        item.status = WeightedAppointmentMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedAppointmentMetricStatus.REQUESTED) }
    }
}
