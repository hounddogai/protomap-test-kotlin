package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledAppointmentVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledAppointmentVariance("code-140", "title-419")
        val view = toScheduledAppointmentVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledAppointmentVarianceViews(mutableListOf<ScheduledAppointmentVariance>(item, item)).size, "views")
    }
}
