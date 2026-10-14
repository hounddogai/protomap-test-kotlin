package com.acme.transport.safety.violation.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalViolationBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalViolationBudget("code-662", "label-469")
        val view = toGlobalViolationBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalViolationBudgetViews(mutableListOf<GlobalViolationBudget>(item, item)).size, "views")
    }
}
