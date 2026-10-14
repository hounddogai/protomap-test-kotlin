package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCoachingBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCoachingBudget("code-602", "label-334")
        val view = toFallbackCoachingBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCoachingBudgetViews(mutableListOf<FallbackCoachingBudget>(item, item)).size, "views")
    }
}
