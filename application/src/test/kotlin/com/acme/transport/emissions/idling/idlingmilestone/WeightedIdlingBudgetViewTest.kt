package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedIdlingBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedIdlingBudget("code-248", "label-20")
        val view = toWeightedIdlingBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedIdlingBudgetViews(mutableListOf<WeightedIdlingBudget>(item, item)).size, "views")
    }
}
