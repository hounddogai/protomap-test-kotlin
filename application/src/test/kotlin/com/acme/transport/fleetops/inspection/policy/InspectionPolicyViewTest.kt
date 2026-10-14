package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionPolicy("code-945", "label-606")
        val view = toInspectionPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionPolicyViews(mutableListOf<InspectionPolicy>(item, item)).size, "views")
    }
}
