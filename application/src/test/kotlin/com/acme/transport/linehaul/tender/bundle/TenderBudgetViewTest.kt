package com.acme.transport.linehaul.tender.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderBudget("code-325", "label-428")
        val view = toTenderBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderBudgetViews(mutableListOf<TenderBudget>(item, item)).size, "views")
    }
}
