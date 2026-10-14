package com.acme.transport.safety.nearmiss.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyNearMissBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyNearMissBudget("code-795", "label-797")
        val view = toLegacyNearMissBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyNearMissBudgetViews(mutableListOf<LegacyNearMissBudget>(item, item)).size, "views")
    }
}
