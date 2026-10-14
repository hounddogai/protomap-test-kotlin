package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientInspectionWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientInspectionWindow("code-69", "label-25")
        val view = toLenientInspectionWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientInspectionWindowViews(mutableListOf<LenientInspectionWindow>(item, item)).size, "views")
    }
}
