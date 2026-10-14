package com.acme.transport.crewing.restbreak.breakpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRestBreakRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRestBreakRule("code-587", "label-103")
        val view = toRegionalRestBreakRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRestBreakRuleViews(mutableListOf<RegionalRestBreakRule>(item, item)).size, "views")
    }
}
