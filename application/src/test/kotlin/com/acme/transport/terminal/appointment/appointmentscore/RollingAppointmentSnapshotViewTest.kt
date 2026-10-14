package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingAppointmentSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingAppointmentSnapshot("code-913", "label-207")
        val view = toRollingAppointmentSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingAppointmentSnapshotViews(mutableListOf<RollingAppointmentSnapshot>(item, item)).size, "views")
    }
}
