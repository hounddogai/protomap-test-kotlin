package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeEntry("code-473", "title-654")
        val view = toTransitTimeEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeEntryViews(mutableListOf<TransitTimeEntry>(item, item)).size, "views")
    }
}
