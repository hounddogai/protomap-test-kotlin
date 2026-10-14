package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingAppointmentBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingAppointmentBundle("code-622", "label-590")
        val view = toRollingAppointmentBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingAppointmentBundleViews(mutableListOf<RollingAppointmentBundle>(item, item)).size, "views")
    }
}
