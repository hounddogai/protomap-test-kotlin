package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterPlan("code-9", "title-454")
        val view = toRosterPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterPlanViews(mutableListOf<RosterPlan>(item, item)).size, "views")
    }
}
