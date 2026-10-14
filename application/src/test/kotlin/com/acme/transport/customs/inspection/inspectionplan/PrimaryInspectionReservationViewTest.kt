package com.acme.transport.customs.inspection.inspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryInspectionReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryInspectionReservation("code-182", "label-364")
        val view = toPrimaryInspectionReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryInspectionReservationViews(mutableListOf<PrimaryInspectionReservation>(item, item)).size, "views")
    }
}
