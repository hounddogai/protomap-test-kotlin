package com.acme.transport.customs.clearance.clearancethreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualClearancePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualClearancePlan("code-360", "name-532")
        val view = toManualClearancePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualClearancePlanViews(mutableListOf<ManualClearancePlan>(item, item)).size, "views")
    }
}
