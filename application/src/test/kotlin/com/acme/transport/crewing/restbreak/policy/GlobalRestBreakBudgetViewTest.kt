package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRestBreakBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRestBreakBudget("code-164", "label-221")
        val view = toGlobalRestBreakBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRestBreakBudgetViews(mutableListOf<GlobalRestBreakBudget>(item, item)).size, "views")
    }
}
