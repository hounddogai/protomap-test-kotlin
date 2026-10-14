package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDropEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDropEntry("code-574", "title-713")
        val view = toPrimaryDropEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDropEntryViews(mutableListOf<PrimaryDropEntry>(item, item)).size, "views")
    }
}
