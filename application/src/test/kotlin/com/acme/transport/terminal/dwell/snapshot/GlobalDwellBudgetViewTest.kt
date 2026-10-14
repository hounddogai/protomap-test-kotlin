package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalDwellBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalDwellBudget("code-937", "label-480")
        val view = toGlobalDwellBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalDwellBudgetViews(mutableListOf<GlobalDwellBudget>(item, item)).size, "views")
    }
}
