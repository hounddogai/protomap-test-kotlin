package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingBudget("code-517", "label-415")
        val view = toCoachingBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingBudgetViews(mutableListOf<CoachingBudget>(item, item)).size, "views")
    }
}
