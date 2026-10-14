package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentNotice("code-370", "name-41")
        val view = toAppointmentNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentNoticeViews(mutableListOf<AppointmentNotice>(item, item)).size, "views")
    }
}
