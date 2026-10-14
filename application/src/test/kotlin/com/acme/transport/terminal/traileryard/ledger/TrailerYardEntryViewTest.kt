package com.acme.transport.terminal.traileryard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardEntry("code-124", "name-624")
        val view = toTrailerYardEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardEntryViews(mutableListOf<TrailerYardEntry>(item, item)).size, "views")
    }
}
