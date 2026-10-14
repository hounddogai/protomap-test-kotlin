package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAppointmentVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAppointmentVariance("code-812", "name-688")
        val view = toPendingAppointmentVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAppointmentVarianceViews(mutableListOf<PendingAppointmentVariance>(item, item)).size, "views")
    }
}
