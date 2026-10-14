package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalAppointmentVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalAppointmentVariance("code-690", "name-780")
        val view = toRegionalAppointmentVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalAppointmentVarianceViews(mutableListOf<RegionalAppointmentVariance>(item, item)).size, "views")
    }
}
