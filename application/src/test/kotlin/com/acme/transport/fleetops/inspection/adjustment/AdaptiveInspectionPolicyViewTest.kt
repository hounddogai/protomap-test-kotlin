package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionPolicy("code-693", "name-880")
        val view = toAdaptiveInspectionPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionPolicyViews(mutableListOf<AdaptiveInspectionPolicy>(item, item)).size, "views")
    }
}
