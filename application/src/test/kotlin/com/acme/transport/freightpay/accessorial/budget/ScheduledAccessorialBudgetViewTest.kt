package com.acme.transport.freightpay.accessorial.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledAccessorialBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledAccessorialBudget("code-7", "title-276")
        val view = toScheduledAccessorialBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledAccessorialBudgetViews(mutableListOf<ScheduledAccessorialBudget>(item, item)).size, "views")
    }
}
