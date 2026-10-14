package com.acme.transport.fleetops.inspection.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionAudit("code-704", "name-893")
        val view = toAdaptiveInspectionAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionAuditViews(mutableListOf<AdaptiveInspectionAudit>(item, item)).size, "views")
    }
}
