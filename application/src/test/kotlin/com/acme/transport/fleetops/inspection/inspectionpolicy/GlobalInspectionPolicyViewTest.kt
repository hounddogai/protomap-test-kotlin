package com.acme.transport.fleetops.inspection.inspectionpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalInspectionPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalInspectionPolicy("code-22", "name-510")
        val view = toGlobalInspectionPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalInspectionPolicyViews(mutableListOf<GlobalInspectionPolicy>(item, item)).size, "views")
    }
}
