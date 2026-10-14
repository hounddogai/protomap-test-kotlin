package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakPlan("code-498", "name-538")
        val view = toRestBreakPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakPlanViews(mutableListOf<RestBreakPlan>(item, item)).size, "views")
    }
}
