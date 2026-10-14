package com.acme.transport.customs.inspection.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingInspectionReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingInspectionReservation("code-846", "name-827")
        val view = toRollingInspectionReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingInspectionReservationViews(mutableListOf<RollingInspectionReservation>(item, item)).size, "views")
    }
}
