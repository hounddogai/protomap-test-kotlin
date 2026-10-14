package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffPlan("code-903", "title-177")
        val view = toCutoffPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffPlanViews(mutableListOf<CutoffPlan>(item, item)).size, "views")
    }
}
