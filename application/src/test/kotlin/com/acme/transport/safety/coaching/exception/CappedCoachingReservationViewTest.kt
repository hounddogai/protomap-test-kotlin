package com.acme.transport.safety.coaching.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCoachingReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCoachingReservation("code-230", "title-808")
        val view = toCappedCoachingReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCoachingReservationViews(mutableListOf<CappedCoachingReservation>(item, item)).size, "views")
    }
}
