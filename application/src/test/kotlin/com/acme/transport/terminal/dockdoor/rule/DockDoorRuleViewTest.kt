package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorRule("code-377", "name-185")
        val view = toDockDoorRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorRuleViews(mutableListOf<DockDoorRule>(item, item)).size, "views")
    }
}
