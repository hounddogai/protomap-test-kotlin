package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentMetric("code-519", "label-683")
        val view = toAppointmentMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentMetricViews(mutableListOf<AppointmentMetric>(item, item)).size, "views")
    }
}
