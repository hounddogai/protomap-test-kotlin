package com.acme.transport.freightpay.accessorial.accessorialbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAccessorialBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAccessorialBudget("code-727", "label-195")
        val view = toManualAccessorialBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAccessorialBudgetViews(mutableListOf<ManualAccessorialBudget>(item, item)).size, "views")
    }
}
