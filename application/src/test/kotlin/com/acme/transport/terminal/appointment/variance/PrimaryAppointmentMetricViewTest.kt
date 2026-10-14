package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryAppointmentMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryAppointmentMetric("code-962", "label-975")
        val view = toPrimaryAppointmentMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryAppointmentMetricViews(mutableListOf<PrimaryAppointmentMetric>(item, item)).size, "views")
    }
}
