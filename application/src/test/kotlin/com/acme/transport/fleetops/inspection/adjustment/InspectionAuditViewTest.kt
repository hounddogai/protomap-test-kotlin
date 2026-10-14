package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionAudit("code-209", "title-201")
        val view = toInspectionAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionAuditViews(mutableListOf<InspectionAudit>(item, item)).size, "views")
    }
}
