package com.acme.transport.terminal.dwell.dwellgroup

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDwellPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDwellPlan("code-620", "name-454")
        val view = toRollingDwellPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDwellPlanViews(mutableListOf<RollingDwellPlan>(item, item)).size, "views")
    }
}
