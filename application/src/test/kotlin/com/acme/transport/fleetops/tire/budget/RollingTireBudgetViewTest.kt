package com.acme.transport.fleetops.tire.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingTireBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingTireBudget("code-737", "title-635")
        val view = toRollingTireBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingTireBudgetViews(mutableListOf<RollingTireBudget>(item, item)).size, "views")
    }
}
