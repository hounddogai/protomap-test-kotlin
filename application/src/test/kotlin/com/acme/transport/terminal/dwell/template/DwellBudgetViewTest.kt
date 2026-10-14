package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellBudget("code-678", "name-879")
        val view = toDwellBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellBudgetViews(mutableListOf<DwellBudget>(item, item)).size, "views")
    }
}
