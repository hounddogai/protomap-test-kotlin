package com.acme.transport.customs.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionPolicy("code-880", "title-131")
        val view = toInspectionPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionPolicyViews(mutableListOf<InspectionPolicy>(item, item)).size, "views")
    }
}
