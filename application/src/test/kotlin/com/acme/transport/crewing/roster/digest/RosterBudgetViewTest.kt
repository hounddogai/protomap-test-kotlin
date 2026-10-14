package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterBudget("code-264", "title-508")
        val view = toRosterBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterBudgetViews(mutableListOf<RosterBudget>(item, item)).size, "views")
    }
}
