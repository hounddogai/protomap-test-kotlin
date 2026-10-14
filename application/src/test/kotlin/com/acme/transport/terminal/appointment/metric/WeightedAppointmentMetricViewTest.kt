package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAppointmentMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAppointmentMetric("code-422", "name-929")
        val view = toWeightedAppointmentMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAppointmentMetricViews(mutableListOf<WeightedAppointmentMetric>(item, item)).size, "views")
    }
}
