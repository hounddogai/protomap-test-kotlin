package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterRequest("code-619", "label-235")
        val view = toRosterRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterRequestViews(mutableListOf<RosterRequest>(item, item)).size, "views")
    }
}
