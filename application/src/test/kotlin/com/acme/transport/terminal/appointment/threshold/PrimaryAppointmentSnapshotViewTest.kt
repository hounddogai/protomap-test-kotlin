package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryAppointmentSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryAppointmentSnapshot("code-625", "label-414")
        val view = toPrimaryAppointmentSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryAppointmentSnapshotViews(mutableListOf<PrimaryAppointmentSnapshot>(item, item)).size, "views")
    }
}
