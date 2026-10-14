package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackNearMissBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackNearMissBudget("code-54", "label-626")
        val view = toFallbackNearMissBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackNearMissBudgetViews(mutableListOf<FallbackNearMissBudget>(item, item)).size, "views")
    }
}
