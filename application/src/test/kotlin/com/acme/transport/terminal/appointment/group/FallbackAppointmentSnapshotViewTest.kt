package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAppointmentSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAppointmentSnapshot("code-486", "label-93")
        val view = toFallbackAppointmentSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAppointmentSnapshotViews(mutableListOf<FallbackAppointmentSnapshot>(item, item)).size, "views")
    }
}
