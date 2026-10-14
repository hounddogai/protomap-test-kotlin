package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveClearanceBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveClearanceBudget("code-141", "label-123")
        val view = toAdaptiveClearanceBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveClearanceBudgetViews(mutableListOf<AdaptiveClearanceBudget>(item, item)).size, "views")
    }
}
