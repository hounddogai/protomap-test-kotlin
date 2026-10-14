package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCoachingPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCoachingPlan("code-451", "title-590")
        val view = toCappedCoachingPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCoachingPlanViews(mutableListOf<CappedCoachingPlan>(item, item)).size, "views")
    }
}
