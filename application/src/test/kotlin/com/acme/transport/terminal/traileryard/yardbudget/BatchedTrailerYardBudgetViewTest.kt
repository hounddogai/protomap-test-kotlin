package com.acme.transport.terminal.traileryard.yardbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTrailerYardBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTrailerYardBudget("code-198", "name-997")
        val view = toBatchedTrailerYardBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTrailerYardBudgetViews(mutableListOf<BatchedTrailerYardBudget>(item, item)).size, "views")
    }
}
