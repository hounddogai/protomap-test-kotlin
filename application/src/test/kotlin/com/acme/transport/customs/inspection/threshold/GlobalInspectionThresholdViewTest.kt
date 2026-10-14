package com.acme.transport.customs.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalInspectionThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalInspectionThreshold("code-729", "title-262")
        val view = toGlobalInspectionThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalInspectionThresholdViews(mutableListOf<GlobalInspectionThreshold>(item, item)).size, "views")
    }
}
