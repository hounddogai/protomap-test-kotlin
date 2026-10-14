package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalInspectionTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalInspectionTemplate("code-77", "name-919")
        val view = toGlobalInspectionTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalInspectionTemplateViews(mutableListOf<GlobalInspectionTemplate>(item, item)).size, "views")
    }
}
