package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterWindow("code-524", "name-45")
        val view = toRosterWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterWindowViews(mutableListOf<RosterWindow>(item, item)).size, "views")
    }
}
