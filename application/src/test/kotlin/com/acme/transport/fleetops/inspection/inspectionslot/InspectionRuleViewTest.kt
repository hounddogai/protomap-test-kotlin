package com.acme.transport.fleetops.inspection.inspectionslot

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionRule("code-535", "label-796")
        val view = toInspectionRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionRuleViews(mutableListOf<InspectionRule>(item, item)).size, "views")
    }
}
