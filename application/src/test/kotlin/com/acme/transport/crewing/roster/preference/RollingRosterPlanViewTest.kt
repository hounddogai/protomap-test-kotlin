package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRosterPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRosterPlan("code-882", "title-316")
        val view = toRollingRosterPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRosterPlanViews(mutableListOf<RollingRosterPlan>(item, item)).size, "views")
    }
}
