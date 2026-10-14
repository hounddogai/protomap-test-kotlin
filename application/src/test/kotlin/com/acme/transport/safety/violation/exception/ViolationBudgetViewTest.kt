package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationBudget("code-67", "name-92")
        val view = toViolationBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationBudgetViews(mutableListOf<ViolationBudget>(item, item)).size, "views")
    }
}
