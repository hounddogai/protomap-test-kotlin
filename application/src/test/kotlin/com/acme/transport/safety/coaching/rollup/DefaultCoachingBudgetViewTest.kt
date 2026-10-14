package com.acme.transport.safety.coaching.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCoachingBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCoachingBudget("code-833", "label-173")
        val view = toDefaultCoachingBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCoachingBudgetViews(mutableListOf<DefaultCoachingBudget>(item, item)).size, "views")
    }
}
