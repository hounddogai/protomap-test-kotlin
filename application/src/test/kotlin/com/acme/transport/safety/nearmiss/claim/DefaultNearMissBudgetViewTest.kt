package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNearMissBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultNearMissBudget("code-63", "name-195")
        val view = toDefaultNearMissBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultNearMissBudgetViews(mutableListOf<DefaultNearMissBudget>(item, item)).size, "views")
    }
}
