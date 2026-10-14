package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class TireRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireRule("code-260", "title-282")
        val view = toTireRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireRuleViews(mutableListOf<TireRule>(item, item)).size, "views")
    }
}
