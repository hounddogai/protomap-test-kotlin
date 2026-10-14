package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredSortationRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredSortationRule("code-963", "title-936")
        val view = toTieredSortationRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredSortationRuleViews(mutableListOf<TieredSortationRule>(item, item)).size, "views")
    }
}
