package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledAppointmentForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledAppointmentForecast("code-716", "title-776")
        val view = toScheduledAppointmentForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledAppointmentForecastViews(mutableListOf<ScheduledAppointmentForecast>(item, item)).size, "views")
    }
}
