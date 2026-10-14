package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientInspectionMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientInspectionMapping("code-174", "title-709")
        val view = toLenientInspectionMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientInspectionMappingViews(mutableListOf<LenientInspectionMapping>(item, item)).size, "views")
    }
}
