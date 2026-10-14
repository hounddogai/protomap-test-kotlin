package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTenderBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTenderBudget("code-697", "title-871")
        val view = toPendingTenderBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTenderBudgetViews(mutableListOf<PendingTenderBudget>(item, item)).size, "views")
    }
}
