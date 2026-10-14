package com.acme.transport.network.cutoff.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCutoffPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCutoffPlan("code-5", "name-916")
        val view = toFallbackCutoffPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCutoffPlanViews(mutableListOf<FallbackCutoffPlan>(item, item)).size, "views")
    }
}
