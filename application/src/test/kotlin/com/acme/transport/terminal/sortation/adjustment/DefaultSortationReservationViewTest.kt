package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultSortationReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultSortationReservation("code-451", "title-854")
        val view = toDefaultSortationReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultSortationReservationViews(mutableListOf<DefaultSortationReservation>(item, item)).size, "views")
    }
}
