package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingNearMissBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingNearMissBudget("code-940", "title-323")
        val view = toPendingNearMissBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingNearMissBudgetViews(mutableListOf<PendingNearMissBudget>(item, item)).size, "views")
    }
}
