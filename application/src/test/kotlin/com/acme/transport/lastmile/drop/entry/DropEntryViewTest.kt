package com.acme.transport.lastmile.drop.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class DropEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropEntry("code-905", "title-991")
        val view = toDropEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropEntryViews(mutableListOf<DropEntry>(item, item)).size, "views")
    }
}
