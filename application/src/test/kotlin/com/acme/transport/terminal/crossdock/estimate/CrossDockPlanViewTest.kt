package com.acme.transport.terminal.crossdock.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockPlan("code-277", "title-973")
        val view = toCrossDockPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockPlanViews(mutableListOf<CrossDockPlan>(item, item)).size, "views")
    }
}
