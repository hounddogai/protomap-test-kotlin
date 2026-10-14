package com.acme.transport.terminal.dwell.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDwellPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDwellPlan("code-659", "title-638")
        val view = toRegionalDwellPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDwellPlanViews(mutableListOf<RegionalDwellPlan>(item, item)).size, "views")
    }
}
