package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRestBreakEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRestBreakEntry("code-728", "name-475")
        val view = toRegionalRestBreakEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRestBreakEntryViews(mutableListOf<RegionalRestBreakEntry>(item, item)).size, "views")
    }
}
