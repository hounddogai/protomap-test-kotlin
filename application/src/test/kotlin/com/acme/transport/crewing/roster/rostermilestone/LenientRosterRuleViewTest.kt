package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientRosterRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientRosterRule("code-248", "title-281")
        val view = toLenientRosterRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientRosterRuleViews(mutableListOf<LenientRosterRule>(item, item)).size, "views")
    }
}
