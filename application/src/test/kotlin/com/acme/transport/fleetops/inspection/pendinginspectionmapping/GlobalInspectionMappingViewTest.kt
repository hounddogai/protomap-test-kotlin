package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalInspectionMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalInspectionMapping("code-524", "name-275")
        val view = toGlobalInspectionMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalInspectionMappingViews(mutableListOf<GlobalInspectionMapping>(item, item)).size, "views")
    }
}
