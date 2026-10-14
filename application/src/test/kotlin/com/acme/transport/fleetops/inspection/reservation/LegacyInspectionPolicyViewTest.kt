package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyInspectionPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyInspectionPolicy("code-52", "name-504")
        val view = toLegacyInspectionPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyInspectionPolicyViews(mutableListOf<LegacyInspectionPolicy>(item, item)).size, "views")
    }
}
