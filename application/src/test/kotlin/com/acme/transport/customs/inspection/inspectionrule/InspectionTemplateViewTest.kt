package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionTemplate("code-435", "name-893")
        val view = toInspectionTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionTemplateViews(mutableListOf<InspectionTemplate>(item, item)).size, "views")
    }
}
