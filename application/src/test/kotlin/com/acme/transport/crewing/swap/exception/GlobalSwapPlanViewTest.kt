package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalSwapPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalSwapPlan("code-237", "name-837")
        val view = toGlobalSwapPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalSwapPlanViews(mutableListOf<GlobalSwapPlan>(item, item)).size, "views")
    }
}
