package com.acme.transport.linehaul.tender.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTenderReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTenderReservation("code-402", "title-277")
        val view = toStrictTenderReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTenderReservationViews(mutableListOf<StrictTenderReservation>(item, item)).size, "views")
    }
}
