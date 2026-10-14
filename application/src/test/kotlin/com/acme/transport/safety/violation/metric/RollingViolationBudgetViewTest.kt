package com.acme.transport.safety.violation.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingViolationBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingViolationBudget("code-780", "title-20")
        val view = toRollingViolationBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingViolationBudgetViews(mutableListOf<RollingViolationBudget>(item, item)).size, "views")
    }
}
