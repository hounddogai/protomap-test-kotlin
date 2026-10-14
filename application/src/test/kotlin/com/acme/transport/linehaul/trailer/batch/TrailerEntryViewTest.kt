package com.acme.transport.linehaul.trailer.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerEntry("code-266", "name-585")
        val view = toTrailerEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerEntryViews(mutableListOf<TrailerEntry>(item, item)).size, "views")
    }
}
