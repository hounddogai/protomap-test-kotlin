package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterRule("code-76", "title-251")
        val view = toRosterRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterRuleViews(mutableListOf<RosterRule>(item, item)).size, "views")
    }
}
