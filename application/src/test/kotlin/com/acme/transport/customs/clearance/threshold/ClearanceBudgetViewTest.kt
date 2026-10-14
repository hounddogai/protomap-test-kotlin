package com.acme.transport.customs.clearance.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceBudget("code-60", "name-662")
        val view = toClearanceBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceBudgetViews(mutableListOf<ClearanceBudget>(item, item)).size, "views")
    }
}
