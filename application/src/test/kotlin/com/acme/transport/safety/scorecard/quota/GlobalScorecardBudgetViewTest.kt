package com.acme.transport.safety.scorecard.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalScorecardBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalScorecardBudget("code-112", "name-281")
        val view = toGlobalScorecardBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalScorecardBudgetViews(mutableListOf<GlobalScorecardBudget>(item, item)).size, "views")
    }
}
