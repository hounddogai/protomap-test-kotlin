package com.acme.transport.terminal.appointment.rollingappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAppointmentVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAppointmentVariance("code-886", "name-301")
        val view = toStrictAppointmentVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAppointmentVarianceViews(mutableListOf<StrictAppointmentVariance>(item, item)).size, "views")
    }
}
