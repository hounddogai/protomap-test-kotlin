package com.acme.transport.terminal.appointment.appointmentpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentManifest("code-558", "name-381")
        val view = toAppointmentManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentManifestViews(mutableListOf<AppointmentManifest>(item, item)).size, "views")
    }
}
