package com.acme.transport.crewing.restbreak.breakrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRestBreakPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRestBreakPlan("code-659", "title-53")
        val view = toScheduledRestBreakPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRestBreakPlanViews(mutableListOf<ScheduledRestBreakPlan>(item, item)).size, "views")
    }
}
