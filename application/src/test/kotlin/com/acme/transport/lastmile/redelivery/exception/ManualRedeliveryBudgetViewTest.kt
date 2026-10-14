package com.acme.transport.lastmile.redelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRedeliveryBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRedeliveryBudget("code-293", "label-490")
        val view = toManualRedeliveryBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRedeliveryBudgetViews(mutableListOf<ManualRedeliveryBudget>(item, item)).size, "views")
    }
}
