package com.acme.transport.linehaul.tender.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTenderPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTenderPlan("code-806", "name-239")
        val view = toFallbackTenderPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTenderPlanViews(mutableListOf<FallbackTenderPlan>(item, item)).size, "views")
    }
}
