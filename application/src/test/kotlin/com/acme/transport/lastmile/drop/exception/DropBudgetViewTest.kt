package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class DropBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropBudget("code-177", "name-412")
        val view = toDropBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropBudgetViews(mutableListOf<DropBudget>(item, item)).size, "views")
    }
}
