package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionGroup("code-105", "name-413")
        val view = toInspectionGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionGroupViews(mutableListOf<InspectionGroup>(item, item)).size, "views")
    }
}
