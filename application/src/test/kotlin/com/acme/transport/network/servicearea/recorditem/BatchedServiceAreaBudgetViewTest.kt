package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedServiceAreaBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedServiceAreaBudget("code-973", "title-357")
        val view = toBatchedServiceAreaBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedServiceAreaBudgetViews(mutableListOf<BatchedServiceAreaBudget>(item, item)).size, "views")
    }
}
