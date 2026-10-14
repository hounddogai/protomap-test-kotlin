package com.acme.transport.safety.coaching.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingPlan("code-846", "name-160")
        val view = toCoachingPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingPlanViews(mutableListOf<CoachingPlan>(item, item)).size, "views")
    }
}
