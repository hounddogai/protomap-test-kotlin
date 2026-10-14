package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDwellBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDwellBudget("code-960", "title-373")
        val view = toDefaultDwellBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDwellBudgetViews(mutableListOf<DefaultDwellBudget>(item, item)).size, "views")
    }
}
