package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionTemplate("code-70", "name-797")
        val view = toInspectionTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionTemplateViews(mutableListOf<InspectionTemplate>(item, item)).size, "views")
    }
}
