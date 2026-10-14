package com.acme.transport.terminal.sortation.strictsortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationReservation("code-718", "title-198")
        val view = toSortationReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationReservationViews(mutableListOf<SortationReservation>(item, item)).size, "views")
    }
}
