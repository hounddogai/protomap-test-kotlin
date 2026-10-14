package com.acme.transport.crewing.roster.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRosterRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRosterRule("code-805", "title-445")
        val view = toStrictRosterRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRosterRuleViews(mutableListOf<StrictRosterRule>(item, item)).size, "views")
    }
}
