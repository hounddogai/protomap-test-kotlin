package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRosterEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRosterEntry("code-248", "name-497")
        val view = toGlobalRosterEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRosterEntryViews(mutableListOf<GlobalRosterEntry>(item, item)).size, "views")
    }
}
