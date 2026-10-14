package com.acme.transport.crewing.roster.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingRosterPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingRosterPlan("code-282", "name-172")
        val view = toPendingRosterPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingRosterPlanViews(mutableListOf<PendingRosterPlan>(item, item)).size, "views")
    }
}
