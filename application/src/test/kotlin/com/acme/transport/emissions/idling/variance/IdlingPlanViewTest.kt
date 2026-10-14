package com.acme.transport.emissions.idling.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingPlan("code-138", "title-193")
        val view = toIdlingPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingPlanViews(mutableListOf<IdlingPlan>(item, item)).size, "views")
    }
}
