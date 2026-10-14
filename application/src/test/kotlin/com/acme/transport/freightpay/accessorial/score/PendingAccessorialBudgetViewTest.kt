package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAccessorialBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAccessorialBudget("code-516", "name-96")
        val view = toPendingAccessorialBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAccessorialBudgetViews(mutableListOf<PendingAccessorialBudget>(item, item)).size, "views")
    }
}
