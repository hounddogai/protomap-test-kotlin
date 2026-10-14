package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRosterGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRosterGroup("code-285", "title-577")
        val view = toGlobalRosterGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRosterGroupViews(mutableListOf<GlobalRosterGroup>(item, item)).size, "views")
    }
}
