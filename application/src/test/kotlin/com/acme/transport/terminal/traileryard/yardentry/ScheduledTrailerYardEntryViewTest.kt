package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTrailerYardEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTrailerYardEntry("code-728", "name-713")
        val view = toScheduledTrailerYardEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTrailerYardEntryViews(mutableListOf<ScheduledTrailerYardEntry>(item, item)).size, "views")
    }
}
