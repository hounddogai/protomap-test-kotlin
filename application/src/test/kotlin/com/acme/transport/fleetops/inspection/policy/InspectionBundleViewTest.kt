package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionBundle("code-630", "name-474")
        val view = toInspectionBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionBundleViews(mutableListOf<InspectionBundle>(item, item)).size, "views")
    }
}
