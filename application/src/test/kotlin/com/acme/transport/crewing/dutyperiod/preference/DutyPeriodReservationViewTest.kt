package com.acme.transport.crewing.dutyperiod.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodReservation("code-681", "title-538")
        val view = toDutyPeriodReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodReservationViews(mutableListOf<DutyPeriodReservation>(item, item)).size, "views")
    }
}
