package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionRollup("code-372", "label-358")
        val view = toInspectionRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionRollupViews(mutableListOf<InspectionRollup>(item, item)).size, "views")
    }
}
