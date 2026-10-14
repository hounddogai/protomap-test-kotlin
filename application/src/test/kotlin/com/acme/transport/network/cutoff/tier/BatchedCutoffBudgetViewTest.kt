package com.acme.transport.network.cutoff.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCutoffBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCutoffBudget("code-204", "title-868")
        val view = toBatchedCutoffBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCutoffBudgetViews(mutableListOf<BatchedCutoffBudget>(item, item)).size, "views")
    }
}
