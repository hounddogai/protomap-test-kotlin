package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class DropRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropRule("code-210", "title-33")
        val view = toDropRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropRuleViews(mutableListOf<DropRule>(item, item)).size, "views")
    }
}
