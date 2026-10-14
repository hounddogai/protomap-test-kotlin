package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryNearMissBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryNearMissBudget("code-348", "title-571")
        val view = toPrimaryNearMissBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryNearMissBudgetViews(mutableListOf<PrimaryNearMissBudget>(item, item)).size, "views")
    }
}
