package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredHubBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredHubBudget("code-713", "title-732")
        val view = toTieredHubBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredHubBudgetViews(mutableListOf<TieredHubBudget>(item, item)).size, "views")
    }
}
