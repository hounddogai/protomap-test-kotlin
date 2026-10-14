package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockRule("code-793", "name-475")
        val view = toCrossDockRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockRuleViews(mutableListOf<CrossDockRule>(item, item)).size, "views")
    }
}
