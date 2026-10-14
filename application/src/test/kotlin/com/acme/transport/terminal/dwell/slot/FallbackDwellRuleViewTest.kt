package com.acme.transport.terminal.dwell.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDwellRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDwellRule("code-896", "title-973")
        val view = toFallbackDwellRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDwellRuleViews(mutableListOf<FallbackDwellRule>(item, item)).size, "views")
    }
}
