package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCoachingBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCoachingBudget("code-142", "label-653")
        val view = toSeasonalCoachingBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCoachingBudgetViews(mutableListOf<SeasonalCoachingBudget>(item, item)).size, "views")
    }
}
