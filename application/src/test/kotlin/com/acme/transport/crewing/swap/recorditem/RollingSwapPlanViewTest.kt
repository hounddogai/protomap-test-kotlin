package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingSwapPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingSwapPlan("code-981", "name-594")
        val view = toRollingSwapPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingSwapPlanViews(mutableListOf<RollingSwapPlan>(item, item)).size, "views")
    }
}
