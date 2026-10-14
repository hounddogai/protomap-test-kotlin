package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedInspectionBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedInspectionBudget("code-48", "title-471")
        val view = toBatchedInspectionBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedInspectionBudgetViews(mutableListOf<BatchedInspectionBudget>(item, item)).size, "views")
    }
}
